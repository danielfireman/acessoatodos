package main

import (
	"encoding/json"
	"log"
	"net/http"
	"os"
	"strconv"
	"time"
	"fmt"

	"github.com/danielfireman/acessoatodos/maps"
	"github.com/danielfireman/acessoatodos/placesdb"
	"github.com/julienschmidt/httprouter"
	"github.com/newrelic/go-agent"
)

const (
	maxRPS                   = 10
	nearbySearchRadiusMeters = 100
	opsTimeout               = 50 * time.Second
	limit                    = 200
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
	gMapsClient, err := maps.DialGoogle(os.Getenv("GOOGLE_MAPS_KEY"), opsTimeout, maxRPS, limit)
	if err != nil {
		log.Fatal(err)
	}

	// DB initialization.
	db, err := placesdb.Dial(os.Getenv("MONGODB_URI"), opsTimeout, maxRPS, limit)
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
		txn := app.StartTransaction("NearbySerch", w, r)
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
		gMapsChan := gMapsClient.NearbySearch(txn, lat, lng, nearbySearchRadiusMeters)

		// Placing db results in a map to ease merging results.
		dbRes, err := db.NearbySearch(txn, lat, lng, nearbySearchRadiusMeters)
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
		if gMapsResp.Error != nil {
			// TODO(danielfireman): Log request
			log.Printf("Error fetching data from GMaps: %q", err)
			http.Error(w, "Invalid lng param", http.StatusInternalServerError)
			return
		}
		var results []NearbySearchResult
		for _, r := range gMapsResp.Results {
			result := NearbySearchResult{
				GoogleMapsPlaceID: r.ID,
				Location: LatLng{
					Lat: r.Lat,
					Lng: r.Lng,
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

		placeID, err := placeID(ps.ByName("id"))
		if err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}

		// Triggering Google Maps request asynchronously.
		gMapsChan := gMapsClient.Get(txn, placeID)

		// Decode request body.
		decoder := json.NewDecoder(r.Body)
		var p PostAccessibilityRequest
		if err := decoder.Decode(&p); err != nil {
			log.Printf("Error decoding request body: %q", err)
			http.Error(w, "Invalid input.", http.StatusBadRequest)
			return
		}
		defer r.Body.Close()

		// Block waiting for Google Maps data.
		gMapsResp := <-gMapsChan
		if gMapsResp.Error != nil {
			log.Printf("Error fetching place details from google maps: %v", err)
			http.Error(w, "Error fetching place details from Google Maps.", http.StatusInternalServerError)
			return
		}
		if len(gMapsResp.Results) == 0 {
			http.Error(w, "Place not found", http.StatusNotFound)
			return
		}
		// Updating database object with information from google maps and from request.
		placeDetails := gMapsResp.Results[0]
		lat := placeDetails.Lat
		lng := placeDetails.Lng
		if err := db.Put(txn, placeID, p.Accessibility, lat, lng); err != nil {
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
		txn := app.StartTransaction("GetPlace", w, r)
		defer txn.End()

		placeID, err := placeID(ps.ByName("id"))
		if err != nil {
			http.Error(w, err.Error(), http.StatusBadRequest)
			return
		}

		// Triggering Google Maps request asynchronously.
		gMapsChan := gMapsClient.Get(txn, placeID)

		// Fetches data from DB.
		dbPlace, err := db.Get(txn, placeID)
		if err != nil {
			// NOTE: Deliberately let the request advance even when we don't had an error trying to fetch
			// accessibility.
			log.Printf("Error fetching place (placeID:%s) details from database: %v", placeID, err)
		}

		// Block waiting for Google Maps data.
		gMapsResp := <-gMapsChan
		if gMapsResp.Error != nil {
			log.Printf("Error fetching place details from google maps: %v", err)
			http.Error(w, "Error fetching place details from Google Maps.", http.StatusServiceUnavailable)
			return
		}
		if len(gMapsResp.Results) == 0 {
			http.Error(w, "Place not found", http.StatusNotFound)
			return
		}
		placeDetails := gMapsResp.Results[0]
		result := GetPlaceResult{
			GoogleMapsPlaceID: placeDetails.ID,
			Location: LatLng{
				Lat: placeDetails.Lat,
				Lng: placeDetails.Lng,
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

func placeID(placeID string) (string, error) {
	if placeID == "" || len(placeID) <= 3 {
		return "", fmt.Errorf("Invalid PlaceID:%s", placeID)
	}
	return placeID[4:], nil
}
