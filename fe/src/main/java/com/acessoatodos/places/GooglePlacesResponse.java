package com.acessoatodos.places;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jooby.Err;
import org.jooby.Status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

class GooglePlacesResponse {

	class GooglePlacesGeometry {
	    Float lat;
	    Float lng;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	class GooglePlacesItem {
		GooglePlacesGeometry geometry;
	    String icon;
	    String id;
	    String name;
	    String place_id;
	    String scope;
	    String reference;
	    String vicinity;
	    ArrayList<String> types;
	}
	
	// TODO(heiner): Remove unused fields.
	private ArrayList<String> html_attributions;
    private ArrayList<GooglePlacesItem> results;
    private String status;
    private String next_page_token;

    static GooglePlacesResponse fromMessage(String message) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return mapper.readValue(message, GooglePlacesResponse.class);
        } catch (IOException e) {
            throw new Err(Status.UNPROCESSABLE_ENTITY, "Erro na conversão dos dados do google places.");
        }
    }

    public List<PlaceVO> toPlacesVO() {
        if (results != null) {
        	List<PlaceVO> places = Lists.newArrayListWithCapacity(results.size());
            for (GooglePlacesItem item : results) {
                PlaceVO placeVO = new PlaceVO();
                placeVO.placeId = item.place_id;
                placeVO.name = item.name;
                placeVO.latitude = item.geometry.lat;
                placeVO.longitude = item.geometry.lng;
                placeVO.types = item.types;
                places.add(placeVO);
            }
        }
        return Lists.newArrayList();
    }
}
