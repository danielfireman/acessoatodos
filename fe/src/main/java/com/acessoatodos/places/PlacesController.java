package com.acessoatodos.places;


import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * This class is responsible to interact with external API, database and return view object.
 */
class PlacesController {
    private GooglePlaces googlePlaces;

    @Inject
    PlacesController(GooglePlaces googlePlaces) {
        this.googlePlaces = googlePlaces;
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
}
