package br.com.acessoatodos.place;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * This class is a model of system to define the attributes that come from routes
 * and define the methods reference this model.
 */
@Getter
@Setter
class PlaceModel {

    /**
     * Latitude position of geolocation point
     */
    private Float latitude;

    /**
     * Longitude position of geolocation point
     */
    private Float longitude;

    protected List<PlaceVO> getNearbyPlaces() {
        PlaceController placeController = new PlaceController();
        return placeController.getNearbyPlaces(this.latitude, this.longitude);
    }
}
