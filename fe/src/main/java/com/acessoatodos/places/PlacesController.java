package com.acessoatodos.places;

import com.acessoatodos.acessibility.Accessibility;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.jooby.Err;
import org.jooby.Status;

import java.util.*;

/**
 * This class is responsible to interact with external API, database and return view object.
 */
class PlacesController {
    private GooglePlaces googlePlaces;
    private DynamoDBMapper mapper;

    private final String PREFIX_GM_PIPE = "gm|";

    @Inject
    PlacesController(GooglePlaces googlePlaces, DynamoDBMapper mapper) {
        this.googlePlaces = googlePlaces;
        this.mapper = mapper;
    }

    List<PlaceVO> getNearbyPlaces(float latitude, float longitude) {
        GooglePlacesResponse response = googlePlaces.nearbySearch(latitude, longitude);

        ArrayList<PlaceTableModel> placesTableModel = Lists.newArrayList();

        if (response.results != null) {
            List<PlaceVO> places = Lists.newArrayListWithCapacity(response.results.size());

            for (GooglePlacesResponse.Item item : response.results) {
                String placeId = PREFIX_GM_PIPE + item.place_id;

                PlaceVO placeVO = new PlaceVO();
                placeVO.placeId = placeId;
                placeVO.name = item.name;
                placeVO.latitude = item.geometry.location.lat;
                placeVO.longitude = item.geometry.location.lng;
                placeVO.types = item.types;
                places.add(placeVO);

                PlaceTableModel placeTableModel = new PlaceTableModel();
                placeTableModel.setPlaceId(placeId);

                placesTableModel.add(placeTableModel);
            }

            List<Object> accessibility = searcAccessibilityOnDbByPlacesIds(placesTableModel);
            return mergeAcessibilities(places, accessibility);
        }

        return Lists.newArrayList();
    }

    public PlaceTableModel insertOrUpdatePlace(String placeId, Set<Integer> acessibilities) {
        String combinedPlaceId = placeId;

        for (Integer accessibility : acessibilities) {
            if (!checkAccessibility(accessibility)) {
                throw new Err(Status.BAD_REQUEST, "Acessibilidade (" + accessibility + ") não contida no sistema.");
            }
        }

        PlaceTableModel placeTableModel = new PlaceTableModel();
        placeTableModel.placeId = combinedPlaceId;
        placeTableModel.acessibilities = Sets.newHashSet(acessibilities);
        mapper.save(placeTableModel);

        return placeTableModel;
    }

    private List<Object> searcAccessibilityOnDbByPlacesIds(List<PlaceTableModel> placesTableModelToSearch) {
        Map<String, List<Object>> stringListMap = mapper.batchLoad(placesTableModelToSearch);

        return stringListMap.get(PlaceTableModel.PLACES_TABLE_NAME);
    }

    private List<PlaceVO> mergeAcessibilities(List<PlaceVO> placeVOs, List<Object> placesTableModel) {
        for (Object object : placesTableModel) {
            PlaceTableModel placeTableModel = (PlaceTableModel) object;
            for (PlaceVO placeVO : placeVOs) {
                if (placeVO.getPlaceId().equals(placeTableModel.getPlaceId())) {
                    placeVO.acessibilities = placeTableModel.getAcessibilities();
                }
            }
        }

        return placeVOs;
    }

    private boolean checkAccessibility(Integer externalValue) {
        for (Accessibility accessibility : Accessibility.values()) {
            if (externalValue == accessibility.getValue()) return true;
        }

        return false;
    }
}
