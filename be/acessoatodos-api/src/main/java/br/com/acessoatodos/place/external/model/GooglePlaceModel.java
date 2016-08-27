package br.com.acessoatodos.place.external.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class GooglePlaceModel {
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