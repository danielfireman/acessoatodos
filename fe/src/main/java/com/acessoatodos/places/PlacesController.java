package com.acessoatodos.places;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.util.HashSet;
import java.util.List;

/**
 * This class is responsible to interact with external API, database and return view object.
 */
class PlacesController {
    private GooglePlaces googlePlaces;
    private DynamoDBMapper mapper;

    @Inject
    PlacesController(GooglePlaces googlePlaces, DynamoDBMapper mapper) {
        this.googlePlaces = googlePlaces;
        this.mapper = mapper;
    }

    List<PlaceVO> getNearbyPlaces(float latitude, float longitude) {
        GooglePlacesResponse response = googlePlaces.nearbySearch(latitude, longitude);

        if (response.results != null) {
            List<PlaceVO> places = Lists.newArrayListWithCapacity(response.results.size());
            for (GooglePlacesResponse.Item item : response.results) {
                PlaceVO placeVO = new PlaceVO();
                placeVO.placeId = item.place_id;
                placeVO.name = item.name;
                placeVO.latitude = item.geometry.location.lat;
                placeVO.longitude = item.geometry.location.lng;
                placeVO.types = item.types;
                places.add(placeVO);
            }
            return places;
        }
        return Lists.newArrayList();
    }

    public PlacesTableModel insertPlace(String placeId, List<Integer> acessibilities) {

        PlacesTableModel placesTableModel = new PlacesTableModel();
        placesTableModel.setPlaceId(placeId);
        placesTableModel.setAcessibilities(new HashSet(acessibilities));

        mapper.save(placesTableModel);

        return placesTableModel;
    }
}
