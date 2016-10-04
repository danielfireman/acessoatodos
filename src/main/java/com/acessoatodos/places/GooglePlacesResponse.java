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
    // TODO(danielfireman): Remove unused fields.
    @JsonProperty("html_attributions") List<String> htmlAttributions;
    @JsonProperty List<Item> results;
    @JsonProperty String status;
    @JsonProperty("next_page_token") String nextPageToken;

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Item {
        @JsonProperty GooglePlacesGeometry geometry;
        @JsonProperty String name;
        @JsonProperty String icon;
        @JsonProperty String id;
        @JsonProperty("place_id") String placeId;
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
