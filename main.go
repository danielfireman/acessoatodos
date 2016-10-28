package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"net/url"
	"os"
	"strconv"

	"googlemaps.github.io/maps"
	"gopkg.in/mgo.v2"
	"gopkg.in/mgo.v2/bson"

	"github.com/julienschmidt/httprouter"
	"github.com/newrelic/go-agent"
	"strings"
)

const (
	mapsQPSLimit             = 10
	nearbySearchRadiusMeters = 100
	PlacesTable              = "places"
)

type PostAccessibilityRequest struct {
	Accessibility []string `json:"accessibility"`
}

type NearbySearchResponse struct {
	Results []NearbySearchResult `json:"results"`
}

type NearbySearchResult struct {
	Name          string   `json:"name"`
	Location      LatLng   `json:"loc"`
	Accessibility []string `json:"accessibility"`
	GoogleMapsPlaceID string `json:"gmplaceid"`
}

type LatLng struct {
	Lat float64 `json:"lat"`
	Lng float64 `json:"lng"`
}

type DBPlace struct {
	ID     bson.ObjectId `bson:"_id,omitempty"`
	GoogleMapsPlaceID     string `bson:"gmplaceid,omitempty"`
	Location      GeoJson  `bson:"loc"`
	Accessibility []string `bson:"acc"`
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
	nrLicence := os.Getenv("NEW_RELIC_LICENSE_KEY")
	if nrLicence == "" {
		log.Fatal("$NEW_RELIC_LICENSE_KEY must be set")
	}
	config := newrelic.NewConfig("acessoatodos", nrLicence)
	app, err := newrelic.NewApplication(config)
	if err != nil {
		log.Fatal(err)
	}
	log.Println("NewRelic monitoring successfully initiated..")

	router := httprouter.New()
	router.GET("/", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
		http.Redirect(w, r, "//github.com/danielfireman/acessoatodos", http.StatusTemporaryRedirect)
	})

	router.GET("/api/v1/nearby", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
		txn := app.StartTransaction("GetPlace", w, r)
		defer txn.End()

		query := r.URL.Query()
		lat, err := strconv.ParseFloat(query.Get("lat"), 64)
		if err != nil {
			log.Printf("Error processing request: %q", err)
			http.Error(w, "Invalid lat param", http.StatusBadRequest)
			return
		}
		lng, err := strconv.ParseFloat(query.Get("lng"), 64)
		if err != nil {
			log.Printf("Error processing request: %q", err)
			http.Error(w, "Invalid lng param", http.StatusBadRequest)
			return
		}

		// Asynchronously fetch data from database.
		mgoDBChan := make(chan map[string]*DBPlace)
		go func(resChan chan<- map[string]*DBPlace) {
			defer newrelic.StartSegment(txn, "MongoDBNearbySearch").End()

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
						"$maxDistance": nearbySearchRadiusMeters,
					},
				},
			})
			err = q.All(&dbRes)
			if err != nil {
				log.Printf("Error fetching data from database: %q", err)
			}
			// Returning a map, which is more suitable to be queried.
			results := make(map[string]*DBPlace, len(dbRes))
			for _, r := range dbRes {
				results[r.GoogleMapsPlaceID] = &r
			}
			resChan <- results
		}(mgoDBChan)

		// Asynchronously sends a request to google maps.
		gMapsChan := make(chan maps.PlacesSearchResponse)
		go func(chan<- maps.PlacesSearchResponse) {
			defer newrelic.StartSegment(txn, "GoogleNearbySearch").End()
			// TODO(danielfireman): add timeout
			resp, err := gMapsClient.NearbySearch(context.Background(), &maps.NearbySearchRequest{
				Location: &maps.LatLng{
					Lat: lat,
					Lng: lng,
				},
				Radius: nearbySearchRadiusMeters,
			})
			if err != nil {
				log.Printf("Error fetching data from GMaps: %q", err)
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
				GoogleMapsPlaceID: r.PlaceID,
				Location: LatLng{
					Lat: r.Geometry.Location.Lat,
					Lng: r.Geometry.Location.Lng,
				},
				Name: r.Name,
			}
			dbEntry, ok := dbResp[result.GoogleMapsPlaceID]
			if ok {
				result.Accessibility = dbEntry.Accessibility
			}
			results = append(results, result)
		}

		w.Header().Set("Content-Type", "application/json")
		if err := json.NewEncoder(w).Encode(NearbySearchResponse{Results: results}); err != nil {
			log.Printf("Error marshaling response: %q", err)
			http.Error(w, "Problems marshaling response.", http.StatusInternalServerError)
		}
	})

	router.PUT("/api/v1/place/:id", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
		txn := app.StartTransaction("PutPlace", w, r)
		defer txn.End()

		placeID := ps.ByName("id")
		if placeID == "" || len(placeID) <= 3 {
			http.Error(w, "Invalid ID.", http.StatusBadRequest)
			return
		}
		// TODO(danielfireman): add timeout
		placeID = placeID[4:]
		result, err := gMapsClient.PlaceDetails(context.Background(), &maps.PlaceDetailsRequest{
			PlaceID:placeID,
		})
		if err != nil {
			log.Printf("Error fetching place details from google maps: %v", err)
			if strings.Contains(err.Error(), "INVALID_REQUEST") {
				http.Error(w, "Invalid ID", http.StatusBadRequest)
				return
			}
			http.Error(w, "Error fetching place details from Google Maps.", http.StatusServiceUnavailable)
			return
		}
		if result.PlaceID == "" {
			log.Printf("Place not found on Google Maps")
			http.Error(w, "Place not found on Google Maps.", http.StatusBadRequest)
			return
		}

		session := mgoSession.Copy()
		defer session.Close()

		var dbPlace DBPlace
		switch session.DB(dbName).C(PlacesTable).Find(bson.M{"gmplaceid": placeID}).Select(bson.M{"_id": 1}).One(&dbPlace) {
		case nil:
		case mgo.ErrNotFound:
			dbPlace.ID = bson.NewObjectId()
		default:
			log.Printf("Error fetching place (placeID:%s) details from database: %v", placeID, err)
			http.Error(w, "Error fetching place details from database.", http.StatusServiceUnavailable)
			return
		}

		decoder := json.NewDecoder(r.Body)
		var p PostAccessibilityRequest
		if err := decoder.Decode(&p); err != nil {
			log.Printf("Error decoding request body: %q", err)
			http.Error(w, "Invalid input.", http.StatusBadRequest)
			return
		}
		defer r.Body.Close()

		// Updating dabase object with information from google maps and from request.
		dbPlace.GoogleMapsPlaceID = placeID
			dbPlace.Accessibility = p.Accessibility
			dbPlace.Location = GeoJson{
				Type:"Point",
				Coordinates:[]float64{result.Geometry.Location.Lat, result.Geometry.Location.Lng},
			}


		if _, err := session.DB(dbName).C(PlacesTable).UpsertId(dbPlace.ID, &dbPlace);  err != nil {
			log.Printf("Error insertind record on database: %q", err)
			http.Error(w, "Problems inserting into database response.", http.StatusInternalServerError)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		if err := json.NewEncoder(w).Encode(p); err != nil {
			log.Printf("Error marshaling response: %q", err)
			http.Error(w, "Problems marshaling response.", http.StatusInternalServerError)
		}
	})

	log.Println("Service listening at port ", port)
	log.Fatal(http.ListenAndServe(":"+port, router))
}
