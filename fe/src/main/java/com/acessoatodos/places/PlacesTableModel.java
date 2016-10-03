package com.acessoatodos.places;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Describes the places table.
 */
@Setter
@Getter
public class PlacesTableModel {
    static final String PLACES_TABLE_NAME = "places";

    String placeId;
    Set<Integer> accessibilities;

    public PlacesTableModel() {
    }

    PlacesTableModel(String placeId) {
        this.placeId = placeId;
    }
}
