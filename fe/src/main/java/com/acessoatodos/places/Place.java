package com.acessoatodos.places;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;
import java.util.Set;

/**
 * API response holding places information.
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Place {
    /**
     * The place id of place registered on google
     */
    String placeId;

    /**
     * The name of place
     */
    String name;

    /**
     * The position of latitude of place
     */
    Float latitude;

    /**
     * The position of longitude of place
     */
    Float longitude;

    /**
     * The list of accessibility codes registered
     */
    Set<Integer> accessibilities;

    /**
     * The list of descriptions of types of places registered on google
     */
    List<String> types;
}
