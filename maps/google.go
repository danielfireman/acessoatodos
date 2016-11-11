package maps

import (
	"fmt"
	"log"
	"strings"
	"time"

	"github.com/newrelic/go-agent"
	"golang.org/x/net/context"
	"googlemaps.github.io/maps"
)

type API struct {
	client  *maps.Client
	timeout time.Duration
	maxRPS  int
	limit   int
}

// Dial connects to the google maps client, specifying a timeout for all DB
// operations and a max number of operations running concurrently.
func DialGoogle(key string, timeout time.Duration, maxRPS, limit int) (*API, error) {
	client, err := maps.NewClient(maps.WithAPIKey(key), maps.WithRateLimit(maxRPS))
	if err != nil {
		return nil, fmt.Errorf("Error creating google maps client: %q", err)
	}
	log.Println("Connected to GoogleMaps")
	return &API{client, timeout, maxRPS, limit}, nil
}

type Place struct {
	Name string
	Lat  float64
	Lng  float64
	ID   string
	Types []string
}

type PlacesResult struct {
	Results []*Place
	Error   error
}

// NearbySearch asynchronously sends a nearby search request to google maps and
// streams back its resources.
func (api *API) NearbySearch(txn newrelic.Transaction, lat, lng float64, radius uint) <-chan PlacesResult {
	resChan := make(chan PlacesResult)
	go func() {
		defer newrelic.StartSegment(txn, "GoogleNearbySearch").End()

		ctx, cancel := context.WithTimeout(context.Background(), api.timeout)
		defer cancel()

		resp, err := api.client.NearbySearch(ctx, &maps.NearbySearchRequest{
			Location: &maps.LatLng{
				Lat: lat,
				Lng: lng,
			},
			Radius: radius,
		})
		if err != nil {
			resChan <- PlacesResult{[]*Place{}, err}
			return
		}
		var places []*Place
		for _, r := range resp.Results {
			places = append(places, &Place{
				Name: r.Name,
				Lat:  r.Geometry.Location.Lat,
				Lng:  r.Geometry.Location.Lng,
				ID:   r.PlaceID,
				Types: r.Types,
			})
		}
		resChan <- PlacesResult{places, nil}
	}()
	return resChan
}

// Get asynchronously fetches information about a place.
func (api *API) Get(txn newrelic.Transaction, placeID string) <-chan PlacesResult {
	resChan := make(chan PlacesResult)
	go func() {
		defer newrelic.StartSegment(txn, "GooglePlaceDetails").End()

		ctx, cancel := context.WithTimeout(context.Background(), api.timeout)
		defer cancel()

		r, err := api.client.PlaceDetails(ctx, &maps.PlaceDetailsRequest{
			PlaceID: placeID,
		})
		if err != nil {
			if strings.Contains(err.Error(), "INVALID_REQUEST") {
				resChan <- PlacesResult{[]*Place{}, nil}
			}
			resChan <- PlacesResult{nil, err}
			return
		}
		resChan <- PlacesResult{
			Results: []*Place{
				{
					Name: r.Name,
					Lat:  r.Geometry.Location.Lat,
					Lng:  r.Geometry.Location.Lng,
					ID:   r.PlaceID,
					Types: r.Types,
				},
			},
			Error: nil,
		}
	}()
	return resChan
}
