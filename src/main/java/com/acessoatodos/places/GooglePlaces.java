package com.acessoatodos.places;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.maps.*;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import org.jooby.Err;
import org.jooby.Status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * Thin wrapper around PlacesApi. Useful when testing.
 */
class GooglePlaces {
    /**
     * Distance to define the radius range to search on google places API. In meters.
     */
    private static final Integer RADIUS_RANGE_METERS = 1000;
    private final GeoApiContext ctx;

    @Inject
    public GooglePlaces(GeoApiContext ctx) {
        this.ctx = ctx;
    }

    NearbySearchRequest nearbySearch(float latitude, float longitude) {
        NearbySearchRequest req = PlacesApi.nearbySearchQuery(ctx, new LatLng(latitude, longitude));
        req.radius(RADIUS_RANGE_METERS);
        return req;
    }

    PendingResult<PlaceDetails> placeDetails(String placeId) {
        return PlacesApi.placeDetails(ctx, placeId);
    }
}
