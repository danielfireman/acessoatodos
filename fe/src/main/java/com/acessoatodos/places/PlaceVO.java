package com.acessoatodos.places;

import java.util.ArrayList;

import com.acessoatodos.acessibility.AcessibilityVO;

import lombok.Getter;

/**
 * API response holding places information.
 */
@Getter
class PlaceVO {
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
     * The list of acessibility codes registered
     */
    ArrayList<AcessibilityVO> acessibilities;

    /**
     * The list of descriptions of types of places registered on google
     */
    ArrayList<String> types;
}
