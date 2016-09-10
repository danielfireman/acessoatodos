package com.acessoatodos.places;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import java.util.*;

/**
 * This class is responsible to interact with external API, database and return view object.
 */
class PlacesController {
    private GooglePlaces googlePlaces;
    private DynamoDBMapper mapper;

    private final String PREFIX_GM_BAR = "gm|";

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
                String placeId = PREFIX_GM_BAR + item.place_id;

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


//        System.out.println("FindBooksPricedLessThanSpecifiedValue: Scan ProductCatalog.");
//
//        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
//        eav.put(":lat", new AttributeValue().withN(value));
//        eav.put(":lng", new AttributeValue().withS("Book"));
//
//        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
//                .withFilterExpression("Price < :val1 and ProductCategory = :val2")
//                .withExpressionAttributeValues(eav);
//
//        List<PlaceTableModel> scanResult = mapper.scan(PlaceTableModel.class, scanExpression);
//
//        for (PlaceTableModel book : scanResult) {
//            System.out.println(book);
//        }

        return Lists.newArrayList();
    }

    public PlaceTableModel insertOrUpdatePlace(String placeId, Set<Integer> acessibilities) {
        String combinedPlaceId = placeId;

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
}
