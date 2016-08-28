package com.acessoatodos.places;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class GooglePlacesItem {
	static class GooglePlacesLocation {
		float lat, lng;
		@JsonCreator GooglePlacesLocation(@JsonProperty("lat") float lat, @JsonProperty("lng") float lng) {
			this.lat = lat;
			this.lng = lng;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	static class GooglePlacesGeometry {
		GooglePlacesLocation location;
		@JsonCreator GooglePlacesGeometry(@JsonProperty("location") GooglePlacesLocation location) {
			this.location = location;
		}
	}

	@JsonProperty GooglePlacesGeometry geometry;
	@JsonProperty String name;
	@JsonProperty String icon;
	@JsonProperty String id;
	@JsonProperty String place_id;
	@JsonProperty String scope;
	@JsonProperty String reference;
	@JsonProperty String vicinity;
	@JsonProperty List<String> types;
}
