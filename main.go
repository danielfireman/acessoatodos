package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"strconv"

	"googlemaps.github.io/maps"

	"strings"
	"time"

	"github.com/danielfireman/acessoatodos/placesdb"
	"github.com/julienschmidt/httprouter"
	"github.com/newrelic/go-agent"
)

const (
	mapsQPSLimit             = 10
	nearbySearchRadiusMeters = 100
	opsTimeout               = 50 * time.Second
)

type PostAccessibilityRequest struct {
	Accessibility []string `json:"accessibility"`
}

type NearbySearchResponse struct {
	Results []NearbySearchResult `json:"results"`
}

type GetPlaceResult struct {
	Name              string   `json:"name"`
	Location          LatLng   `json:"loc"`
	Accessibility     []string `json:"accessibility"`
	GoogleMapsPlaceID string   `json:"gmplaceid"`
}

type NearbySearchResult struct {
	Name              string   `json:"name"`
	Location          LatLng   `json:"loc"`
	Accessibility     []string `json:"accessibility"`
	GoogleMapsPlaceID string   `json:"gmplaceid"`
}

type LatLng struct {
	Lat float64 `json:"lat"`
	Lng float64 `json:"lng"`
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

	// DB
	db, err := placesdb.Dial(os.Getenv("MONGODB_URI"), opsTimeout)
	if err != nil {
		log.Fatal(err)
	}

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

		// Placing db results in a map to ease merging results.
		dbSeg := newrelic.StartSegment(txn, "DBNearbySearch")
		dbRes, err := db.NearbySearch(lat, lng, nearbySearchRadiusMeters)
		dbSeg.End()
		if err != nil {
			// NOTE: Deliberately let the request advance even when we don't had an error trying to fetch
			// accessibility.
			// TODO(danielfireman): Log request.
			log.Printf("Error fetching data from database: %q", err)
		}

		dbResMap := make(map[string]*placesdb.Place, len(dbRes))
		for _, r := range dbRes {
			dbResMap[r.GoogleMapsPlaceID] = &r
		}
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
			dbEntry, ok := dbResMap[result.GoogleMapsPlaceID]
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
		// TODO(danielfireman): Trigger this from an goroutine blocking only after fetching the objectID
		// TODO(danielfireman): Pass-in a cancellable context.
		placeID = placeID[4:]
		result, err := gMapsClient.PlaceDetails(context.Background(), &maps.PlaceDetailsRequest{
			PlaceID: placeID,
		})
		if err != nil {
			if strings.Contains(err.Error(), "INVALID_REQUEST") {
				http.Error(w, "Place not found", http.StatusNotFound)
				return
			}
			log.Printf("Error fetching place details from google maps: %v", err)
			http.Error(w, "Error fetching place details from Google Maps.", http.StatusServiceUnavailable)
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

		// Updating database object with information from google maps and from request.
		lat := result.Geometry.Location.Lat
		lng := result.Geometry.Location.Lng
		if err := db.Upsert(placeID, p.Accessibility, lat, lng); err != nil {
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

	router.GET("/api/v1/place/:id", func(w http.ResponseWriter, r *http.Request, ps httprouter.Params) {
		placeID := ps.ByName("id")
		if placeID == "" || len(placeID) <= 3 {
			http.Error(w, "Invalid ID.", http.StatusBadRequest)
			return
		}
		placeID = placeID[4:]

		// TODO(danielfireman): add timeout
		// TODO(danielfireman): Trigger this from an goroutine blocking only after fetching the objectID
		// TODO(danielfireman): Pass-in a cancellable context.
		placeDetails, err := gMapsClient.PlaceDetails(context.Background(), &maps.PlaceDetailsRequest{
			PlaceID: placeID,
		})
		if err != nil {
			if strings.Contains(err.Error(), "INVALID_REQUEST") {
				http.Error(w, "Place not found", http.StatusNotFound)
				return
			}
			log.Printf("Error fetching place details from google maps: %v", err)
			http.Error(w, "Error fetching place details from Google Maps.", http.StatusServiceUnavailable)
			return
		}

		dbPlace, err := db.Get(placeID)
		if err != nil {
			log.Printf("Error fetching place (placeID:%s) details from database: %v", placeID, err)
			http.Error(w, "Error fetching place details from database.", http.StatusServiceUnavailable)
			return
		}

		result := GetPlaceResult{
			GoogleMapsPlaceID: placeDetails.PlaceID,
			Location: LatLng{
				Lat: placeDetails.Geometry.Location.Lat,
				Lng: placeDetails.Geometry.Location.Lng,
			},
			Name:          placeDetails.Name,
			Accessibility: dbPlace.Accessibility,
		}
		w.Header().Set("Content-Type", "application/json")
		if err := json.NewEncoder(w).Encode(result); err != nil {
			log.Printf("Error marshaling response: %q", err)
			http.Error(w, "Problems marshaling response.", http.StatusInternalServerError)
		}

	})
	log.Println("Service listening at port ", port)
	log.Fatal(http.ListenAndServe(":"+port, router))
}
