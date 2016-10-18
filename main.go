package main

import (
	"context"
	"log"
	"os"
	"strconv"
	"time"

	"github.com/kataras/iris"
	"github.com/yvasiyarov/go-metrics"
	"github.com/yvasiyarov/gorelic"
	"googlemaps.github.io/maps"
)

const (
	mapsQPSLimit             = 10
	nearbySearchRadiusMeters = 100
)

var agent *gorelic.Agent

func gorelicMon(ctx *iris.Context) {
	startTime := time.Now()
	ctx.Next()
	agent.HTTPTimer.UpdateSince(startTime)
	c, ok := agent.HTTPStatusCounters[ctx.Response.StatusCode()]
	if !ok {
		c = metrics.NewCounter()
		agent.HTTPStatusCounters[ctx.Response.StatusCode()] = c
	}
	c.Inc(1)
}

func initNewrelicAgent(license string, appname string) error {
	agent = gorelic.NewAgent()
	agent.NewrelicLicense = license
	agent.NewrelicName = appname

	agent.HTTPTimer = metrics.NewTimer()
	agent.CollectHTTPStat = true
	agent.CollectHTTPStatuses = true
	agent.CollectMemoryStat = true
	agent.Verbose = true

	agent.Run()
	return nil
}

func main() {
	// Environment validation. We would like to fail fast.
	port := os.Getenv("PORT")
	if port == "" {
		log.Fatal("$PORT env var is mandatory")
	}

	gMapsKey := os.Getenv("GOOGLE_MAPS_KEY")
	if gMapsKey == "" {
		log.Fatal("$GOOGLE_MAPS_KEY env var is mandatory")
	}

	gMapsClient, err := maps.NewClient(maps.WithAPIKey(gMapsKey), maps.WithRateLimit(mapsQPSLimit))
	if err != nil {
		log.Fatalf("Error creating google maps client: %q", err)
	}

	nRelicLicense := os.Getenv("NEW_RELIC_LICENSE_KEY")
	if nRelicLicense == "" {
		log.Fatal("NEW_RELIC_LICENSE_KEY env var is mandatory")
	}
	if err := initNewrelicAgent(nRelicLicense, "acessoatodos"); err != nil {
		log.Fatalf("Error starting NewRelic agent: %q\n", err)
	}

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
				ctx.HTML(iris.StatusBadRequest, "Invalid lat param")
				return
			}
			lng, err := strconv.ParseFloat(ctx.URLParam("lng"), 64)
			if err != nil {
				ctx.Logger().Printf("Error processing request: %q", err)
				ctx.HTML(iris.StatusBadRequest, "Invalid lng param")
				return
			}

			nearbySearchTracer := agent.Tracer.BeginTrace("GoogleNearbySearch")
			resp, err := gMapsClient.NearbySearch(context.Background(), &maps.NearbySearchRequest{
				Location: &maps.LatLng{
					Lat: lat,
					Lng: lng,
				},
				Radius: nearbySearchRadiusMeters,
			})
			nearbySearchTracer.EndTrace()

			if err != nil {
				ctx.Logger().Printf("Error processing request: %q", err)
				ctx.SetStatusCode(iris.StatusInternalServerError)
				return
			}
			ctx.SetContentType("application/json")
			ctx.JSON(iris.StatusOK, resp)
		})
	}
	app.Listen(":" + port)
}
