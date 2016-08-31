/**
 * This copy of Woodstox XML processor is licensed under the
 * Apache (Software) License, version 2.0 ("the License").
 * See the License for details about distribution rights, and the
 * specific rights regarding derivate works.
 *
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing Woodstox, in file "ASL2.0", under the same directory
 * as this file.
 */
package com.acessoatodos.places;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * This class is responsible to interact with external API, database and return view object. */
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
