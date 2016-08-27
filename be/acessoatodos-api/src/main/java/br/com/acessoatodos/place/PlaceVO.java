package br.com.acessoatodos.place;

import br.com.acessoatodos.acessibility.AcessibilityVO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * This class is used by create a model object to return to front application
 */
@Getter
@Setter
public class PlaceVO {
    /**
     * The place id of place registered on google
     */
    private String placeId;

    /**
     * The name of place
     */
    private String name;

    /**
     * The position of latitude of place
     */
    private Float latitude;

    /**
     * The position of longitude of place
     */
    private Float longitude;

    /**
     * The list of acessibility code registered on acessoatodos database
     */
    private ArrayList<AcessibilityVO> acessibilities;

    /**
     * The list of descriptions of types of places registered on google
     */
    private ArrayList<String> types;
}
