package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"strconv"

	"github.com/kataras/iris"

	"googlemaps.github.io/maps"
)

const (
	mapsQPSLimit             = 10
	nearbySearchRadiusMeters = 100
)

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

	app := iris.New()
	apiV1 := app.Party("api/v1")
	{
		apiV1.Get("/place", func(ctx *iris.Context) {
			lat, err := strconv.ParseFloat(ctx.URLParam("lat"), 64)
			if err != nil {
				ctx.Logger().Printf("Error processing request: %q", err)
				ctx.HTML(http.StatusBadRequest, "Invalid lat param")
				return
			}
			lng, err := strconv.ParseFloat(ctx.URLParam("lng"), 64)
			if err != nil {
				ctx.Logger().Printf("Error processing request: %q", err)
				ctx.HTML(http.StatusBadRequest, "Invalid lng param")
				return
			}
			resp, err := gMapsClient.NearbySearch(context.Background(), &maps.NearbySearchRequest{
				Location: &maps.LatLng{
					Lat: lat,
					Lng: lng,
				},
				Radius: nearbySearchRadiusMeters,
			})
			if err != nil {
				ctx.Logger().Printf("Error processing request: %q", err)
				ctx.SetStatusCode(http.StatusInternalServerError)
			}
			ctx.SetContentType("application/json")
			ctx.JSON(http.StatusOK, resp)
		})
	}
	app.Listen(":" + port)
}