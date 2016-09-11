package com.acessoatodos.places;

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
    private static final Map<Integer, String> accessibility;

    @Inject
    PlacesController(GooglePlaces googlePlaces, DynamoDBMapper mapper) {
        this.googlePlaces = googlePlaces;
        this.mapper = mapper;
    }

    List<PlaceVO> getNearbyPlaces(float latitude, float longitude) {
        GooglePlacesResponse response = googlePlaces.nearbySearch(latitude, longitude);

        ArrayList<PlaceTableModel> placesTableModel = new ArrayList();

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

            return searcAcessibilitiesOnDb(placesTableModel, places);
        }

        return Lists.newArrayList();
    }

    public PlaceTableModel insertOrUpdatePlace(String placeId, Set<Integer> acessibilities) {
        String combinedPlaceId = placeId;

        for (Integer accessibility : acessibilities) {
            if(!checkAccessibility(accessibility)) {
                throw new Err(Status.BAD_REQUEST, "Acessibilidade (" + accessibility +") não contida no sistema.");
            }
        }

        PlaceTableModel placeTableModel = new PlaceTableModel();
        placeTableModel.placeId = combinedPlaceId;
        placeTableModel.acessibilities = Sets.newHashSet(acessibilities);
        mapper.save(placeTableModel);

        return placeTableModel;
    }

    private List<PlaceVO> searcAcessibilitiesOnDb(List<PlaceTableModel> placesTableModelToSearch, List<PlaceVO> placeVOs) {
        Map<String, List<Object>> stringListMap = mapper.batchLoad(placesTableModelToSearch);

        //TODO(Heiner) refactor
        for (Map.Entry<String, List<Object>> map : stringListMap.entrySet()) {
            for (Object object: map.getValue()) {
                PlaceTableModel placeTableModel = (PlaceTableModel) object;
                for (PlaceVO placeVO : placeVOs) {
                    if(placeVO.getPlaceId().equals(placeTableModel.getPlaceId())) {
                        placeVO.acessibilities = placeTableModel.getAcessibilities();
                    }
                }
            }
        }

        return placeVOs;
    }

    private boolean checkAccessibility(Integer externalValue) {
        return (this.accessibility.get(externalValue) != null) ? true : false;
    }

    static {
        Map<Integer, String> aMap = new HashMap<>();
        aMap.put(100, "Access ramp");
        aMap.put(101, "Adapted WC");
        aMap.put(102, "Elevator");
        aMap.put(103, "Panel braile elevator");
        aMap.put(104, "Info braile");
        accessibility = Collections.unmodifiableMap(aMap);
    }
}
