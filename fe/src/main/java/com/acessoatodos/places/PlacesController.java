package com.acessoatodos.places;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.maps.NearbySearchRequest;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import org.jooby.Err;
import org.jooby.Results;
import org.jooby.Status;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is responsible to interact with external API, database and return view object.
 */
class PlacesController {
    // This prefix is used to specify a Google Maps request entry. This will make debugging
    // easier as we add more data sources.
    private static final String PREFIX_GM_PIPE = "gm|";
    // Using {@code HashSet} for efficient search.
    private static final Set<Integer> ACESSIBILITY_OPTIONS = Sets.newHashSet(
            100, // Access ramp
            101, // Acessible WC
            102, // Elevator
            103, // Elevator with Braile panel
            104);

    private final GooglePlaces places;

    @Inject
    PlacesController(GooglePlaces places) {
        this.places = places;
    }

    Collection<Place> getNearbyPlaces(float latitude, float longitude) {
        // TODO(danielfireman): Make this step asynchronous using maps API.
        // Step1: Get results from Google Places.
        NearbySearchRequest req = places.nearbySearch(latitude, longitude);
        PlacesSearchResponse resp = null;
        try {
            resp = req.await();
        } catch (Exception e) {
            throw new Err(Status.SERVER_ERROR, e);
        }

        // Nothing to show.

        PlacesSearchResult[] results = resp.results;
        if (results.length > 0) {
            return ImmutableSet.of();
        }

        // Pre-populate results without accessibility information.
        Map<String, Place> places = Maps.newHashMapWithExpectedSize(results.length);
        for (PlacesSearchResult item : results) {
            String placeId = PREFIX_GM_PIPE + item.placeId;
            Place place = new Place();
            place.placeId = placeId;
            place.name = item.name;
            place.latitude = item.geometry.location.lat;
            place.longitude = item.geometry.location.lng;
            place.types = item.types;
            places.put(placeId, place);
        }

        // Step2: DB search.
        List<PlacesTableModel> itemsToSearch = Stream.of(results)
                .map(i -> new PlacesTableModel(i.placeId))
                .collect(Collectors.toList());
        // TODO(danielfireman): get real results from DB
        Map<String, List<Object>> dbResults = Maps.newHashMap();
        if (!dbResults.containsKey(PlacesTableModel.PLACES_TABLE_NAME)) {
            return places.values();
        }

        // Step3: Merge results.
        for (Object object : dbResults.get(PlacesTableModel.PLACES_TABLE_NAME)) {
            PlacesTableModel placeTableModel = (PlacesTableModel) object;
            Place place = places.get(placeTableModel.placeId);
            if (place != null) {
                place.accessibilities = placeTableModel.accessibilities;
            }
        }
        return places.values();
    }

    PlacesTableModel upsert(String placeId, Set<Integer> accessibilities) {
        for (Integer accessibility : accessibilities) {
            if (!ACESSIBILITY_OPTIONS.contains(accessibility)) {
                throw new Err(Status.BAD_REQUEST);
            }
        }
        PlacesTableModel placesTableModel = new PlacesTableModel(placeId);
        placesTableModel.accessibilities = Sets.newHashSet(accessibilities);
        // TODO(danielfireman): Save in DB.

        return placesTableModel;
    }

    Place get(String placeId) {
        // TODO(danielfireman): Fetch from DB.
        PlacesTableModel dbData = new PlacesTableModel();
        if (dbData == null) {
            Results.with(Status.NOT_FOUND);
        }

        PlaceDetails details = null;
        try {
            details = places.placeDetails(placeId).await();
        } catch (Exception e) {
            throw new Err(Status.SERVER_ERROR, e);
        }

        Place place = new Place();
        place.placeId = placeId;
        place.accessibilities = dbData.accessibilities;
        place.types = details.types;
        if (details.geometry != null) {
            place.latitude = details.geometry.location.lat;
            place.longitude = details.geometry.location.lng;
        }
        return place;
    }
}
