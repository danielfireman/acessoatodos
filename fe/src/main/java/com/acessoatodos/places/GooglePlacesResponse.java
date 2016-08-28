package com.acessoatodos.places;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;

@JsonIgnoreProperties(ignoreUnknown = true)
class GooglePlacesResponse {
	// TODO(heiner): Remove unused fields.
	public List<String> html_attributions;
	public List<GooglePlacesItem> results = Lists.newArrayList();
	public String status;
	public String next_page_token;
	
	public List<GooglePlacesItem> addItem(GooglePlacesItem item) {
		results.add(item);
		return results;
	}

    public List<PlaceVO> toPlacesVO() {
        if (results != null) {
        	List<PlaceVO> places = Lists.newArrayListWithCapacity(results.size());
            for (GooglePlacesItem item : results) {
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
