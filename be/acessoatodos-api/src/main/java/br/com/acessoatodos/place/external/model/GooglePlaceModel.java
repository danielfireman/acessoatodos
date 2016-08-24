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

//        "opening_hours":{
//          "open_now":true
//        },
//        "photos":[
    //        {
    //        "height":270,
    //        "html_attributions":[],
    //        "photo_reference":"CnRnAAAAF-LjFR1ZV93eawe1cU_3QNMCNmaGkowY7CnOf-kcNmPhNnPEG9W979jOuJJ1sGr75rhD5hqKzjD8vbMbSsRnq_Ni3ZIGfY6hKWmsOf3qHKJInkm4h55lzvLAXJVc-Rr4kI9O1tmIblblUpg2oqoq8RIQRMQJhFsTr5s9haxQ07EQHxoUO0ICubVFGYfJiMUPor1GnIWb5i8",
    //        "width":519
    //        }
//        ],
//        "alt_ids":[
    //        {
    //        "place_id":"D9iJyWEHuEmuEmsRm9hTkapTCrk",
    //        "scope":"APP"
    //        }
//        ],
