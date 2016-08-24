package br.com.acessoatodos.place;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by k-heiner@hotmail.com on 22/08/2016.
 */
@Getter
@Setter
public class PlaceModel {

    private Float latitude;
    private Float longitude;

    protected List<PlaceVO> getNearbyPlaces() {
        PlaceController placeController = new PlaceController();
        return placeController.getNearbyPlaces(this.latitude, this.longitude);
    }
}
