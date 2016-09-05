package com.acessoatodos.places;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

/**
 * Data holder for Google Places responses.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class GooglePlacesResponse {
    // TODO(heiner): Remove unused fields.
    @JsonProperty List<String> html_attributions;
    @JsonProperty List<Item> results = Lists.newArrayList();
    @JsonProperty String status;
    @JsonProperty String next_page_token;

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Item {
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GooglePlacesGeometry {
        @JsonProperty GooglePlacesLocation location;
    }

    static class GooglePlacesLocation {
        @JsonProperty float lat;
        @JsonProperty float lng;
    }
}
