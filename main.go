package main

import (
	"context"
	"log"
	"net/url"
	"os"
	"strconv"
	"time"

	"fmt"

	"github.com/kataras/iris"
	"github.com/yvasiyarov/go-metrics"
	"github.com/yvasiyarov/gorelic"
	"googlemaps.github.io/maps"
	"gopkg.in/mgo.v2"
	"gopkg.in/mgo.v2/bson"
)

const (
	mapsQPSLimit             = 10
	nearbySearchRadiusMeters = 100
	PlacesTable              = "places"
)

var monitoringAgent *gorelic.Agent

func gorelicMon(ctx *iris.Context) {
	startTime := time.Now()
	ctx.Next()
	monitoringAgent.HTTPTimer.UpdateSince(startTime)
	c, ok := monitoringAgent.HTTPStatusCounters[ctx.Response.StatusCode()]
	if !ok {
		c = metrics.NewCounter()
		monitoringAgent.HTTPStatusCounters[ctx.Response.StatusCode()] = c
	}
	c.Inc(1)
}

func initNewrelicAgent(license string, appname string) error {
	monitoringAgent = gorelic.NewAgent()
	monitoringAgent.NewrelicLicense = license
	monitoringAgent.NewrelicName = appname

	monitoringAgent.HTTPTimer = metrics.NewTimer()
	monitoringAgent.CollectHTTPStat = true
	monitoringAgent.CollectHTTPStatuses = true
	monitoringAgent.CollectMemoryStat = true
	monitoringAgent.Verbose = true

	monitoringAgent.Run()
	return nil
}

type NearbySearchResponse struct {
	Results []NearbySearchResult `json:"results"`
}

type NearbySearchResult struct {
	ID            string   `json:"id"`
	Accessibility []string `json:"accessibility"`
	Name          string   `json:"name"`
	Location      LatLng   `json:"loc"`
}

type LatLng struct {
	Lat float64 `json:"lat"`
	Lng float64 `json:"lng"`
}

type DBPlace struct {
	ID            string   `bson:"id,omitempty"`
	Accessibility []string `bson:"accessibility"`
	Location      GeoJson  `bson:"loc"`
}

type GeoJson struct {
	Type        string    `bson:"type,omitempty"`
	Coordinates []float64 `bson:"coordinates"`
}

func main() {
	// Environment validation. We would like to fail fast.
	port := os.Getenv("PORT")
	if port == "" {
		log.Fatal("$PORT env var is mandatory")
	}

	// Google Maps initialization.
	gMapsKey := os.Getenv("GOOGLE_MAPS_KEY")
	if gMapsKey == "" {
		log.Fatal("$GOOGLE_MAPS_KEY env var is mandatory")
	}
	gMapsClient, err := maps.NewClient(maps.WithAPIKey(gMapsKey), maps.WithRateLimit(mapsQPSLimit))
	if err != nil {
		log.Fatalf("Error creating google maps client: %q", err)
	}
	fmt.Println("Connected to GoogleMaps")

	// MongoDB initialization.
	mgoURL, err := url.Parse(os.Getenv("MONGODB_URI"))
	if mgoURL.String() == "" || err != nil {
		log.Fatalf("Error parsing MONGDB_URI:%s err:%q\n", mgoURL.String(), err)
	}
	mgoSession, err := mgo.Dial(mgoURL.String())
	if err != nil {
		log.Fatalf("Error connecting to mongoDB:%s err:%q\n", mgoURL.String(), err)
	}
	dbName := mgoURL.EscapedPath()[1:] // Removing initial "/"
	fmt.Printf("Connected to mongo. DB:%s URI:%s\n", dbName, mgoURL)

	// New relic initialization.
	nRelicLicense := os.Getenv("NEW_RELIC_LICENSE_KEY")
	if nRelicLicense == "" {
		log.Fatal("NEW_RELIC_LICENSE_KEY env var is mandatory")
	}
	if err := initNewrelicAgent(nRelicLicense, "acessoatodos"); err != nil {
		log.Fatalf("Error starting NewRelic agent: %q\n", err)
	}
	fmt.Println("New relic agent initiated.")

	app := iris.New()
	// Redirecting to github for now.
	app.Get("/", func(ctx *iris.Context) {
		ctx.Redirect("//github.com/danielfireman/acessoatodos", iris.StatusTemporaryRedirect)
	})

	apiV1 := app.Party("api/v1")
	{
		apiV1.UseFunc(gorelicMon)
		apiV1.Get("/place", func(ctx *iris.Context) {
			lat, err := strconv.ParseFloat(ctx.URLParam("lat"), 64)
			if err != nil {
				ctx.Logger().Printf("Error processing request: %q", err)
				ctx.Error("Invalid lat param", iris.StatusBadRequest)
				return
			}
			lng, err := strconv.ParseFloat(ctx.URLParam("lng"), 64)
			if err != nil {
				ctx.Logger().Printf("Error processing request: %q", err)
				ctx.Error("Invalid lng param", iris.StatusBadRequest)
				return
			}

			// Asynchronously fetch data from database.
			mgoDBChan := make(chan map[string]*DBPlace)
			go func(resChan chan<- map[string]*DBPlace) {
				session := mgoSession.Copy()
				defer session.Close()
				c := session.DB(dbName).C(PlacesTable)
				var dbRes []DBPlace
				q := c.Find(bson.M{
					"loc": bson.M{
						"$nearSphere": bson.M{
							"$geometry": bson.M{
								"type":        "Point",
								"coordinates": []float64{lat, lng},
							},
							"$maxDistance": 1000,
						},
					},
				})
				err = q.All(&dbRes)
				if err != nil {
					ctx.Logger().Printf("Error fetching data from database: %q", err)
				}
				// Returning a map, which is more suitable to be queried.
				results := make(map[string]*DBPlace, len(dbRes))
				for _, r := range dbRes {
					results[r.ID] = &r
				}
				resChan <- results
			}(mgoDBChan)

			// Asynchronously sends a request to google maps.
			gMapsChan := make(chan maps.PlacesSearchResponse)
			go func(chan<- maps.PlacesSearchResponse) {
				defer monitoringAgent.Tracer.BeginTrace("GoogleNearbySearch").EndTrace()
				resp, err := gMapsClient.NearbySearch(context.Background(), &maps.NearbySearchRequest{
					Location: &maps.LatLng{
						Lat: lat,
						Lng: lng,
					},
					Radius: nearbySearchRadiusMeters,
				})
				if err != nil {
					ctx.Logger().Printf("Error fetching data from GMaps: %q", err)
					gMapsChan <- maps.PlacesSearchResponse{}
					return
				}
				gMapsChan <- resp
			}(gMapsChan)

			// Merging results.
			dbResp := <-mgoDBChan
			gMapsResp := <-gMapsChan
			var results []NearbySearchResult
			for _, r := range gMapsResp.Results {
				result := NearbySearchResult{
					ID: "gm/" + r.PlaceID,
					Location: LatLng{
						Lat: r.Geometry.Location.Lat,
						Lng: r.Geometry.Location.Lng,
					},
					Name: r.Name,
				}
				dbEntry, ok := dbResp[result.ID]
				if ok {
					result.Accessibility = dbEntry.Accessibility
				}
				results = append(results, result)
			}
			ctx.SetContentType("application/json")
			ctx.JSON(iris.StatusOK, NearbySearchResponse{Results: results})
		})
	}
	app.Listen(":" + port)
}
