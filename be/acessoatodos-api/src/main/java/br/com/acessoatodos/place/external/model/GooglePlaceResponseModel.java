package br.com.acessoatodos.place.external.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * Created by k-heiner@hotmail.com on 23/08/2016.
 */
@Getter
@Setter
public class GooglePlaceResponseModel {
    private ArrayList<String> html_attributions;
    private ArrayList<GooglePlaceModel> results;
    private String status;
    private String next_page_token;
}
