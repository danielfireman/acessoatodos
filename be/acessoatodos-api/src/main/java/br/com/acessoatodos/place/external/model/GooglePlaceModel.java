package br.com.acessoatodos.place.external.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * Created by k-heiner@hotmail.com on 23/08/2016.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GooglePlaceModel {
    private GoogleGeometryModel geometry;
    private String icon;
    private String id;
    private String name;
    private String place_id;
    private String scope;
    private String reference;
    private String vicinity;
    private ArrayList<String> types;
}