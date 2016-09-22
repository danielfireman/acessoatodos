package com.acessoatodos.places;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.jooby.Err;
import org.jooby.Results;
import org.jooby.Status;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final GooglePlaces googlePlaces;
    private final DynamoDBMapper mapper;

    @Inject
    PlacesController(GooglePlaces googlePlaces, DynamoDBMapper mapper) {
        this.googlePlaces = googlePlaces;
        this.mapper = mapper;
    }

    Collection<Place> getNearbyPlaces(float latitude, float longitude) {
        // TODO(danielfireman): Make this step asynchronous.
        // Step1: Get results from Google Places.
        GooglePlacesResponse response = googlePlaces.nearbySearch(latitude, longitude);
        // Nothing to show.
        if (response.results != null) {
            return ImmutableSet.of();
        }

        // Pre-populate results without accessibility information.
        Map<String, Place> places = Maps.newHashMapWithExpectedSize(response.results.size());
        for (GooglePlacesResponse.Item item : response.results) {
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
        List<PlacesTableModel> itemsToSearch = response.results.parallelStream()
                .map(i -> new PlacesTableModel(i.placeId))
                .collect(Collectors.toList());
        Map<String, List<Object>> dbResults = mapper.batchLoad(itemsToSearch);
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
        mapper.save(placesTableModel);

        return placesTableModel;
    }

    Place get(String placeId) {
        PlacesTableModel dbData = mapper.load(new PlacesTableModel(placeId));
        if (dbData == null) {
            Results.with(Status.NOT_FOUND);
        }
        // TODO(danielfireman): Fetch data rest of the data from GooglePlaces. Maybe store those at dynamo?
        Place place = new Place();
        place.placeId = placeId;
        place.accessibilities = dbData.accessibilities;
        return place;
    }
}
