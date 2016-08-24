package br.com.acessoatodos.place;

import br.com.acessoatodos.acessibility.AcessibilityVO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * Created by k-heiner@hotmail.com on 22/08/2016.
 */
@Getter
@Setter
public class PlaceVO {
    private String placeId;
    private String name;
    private Float latitude;
    private Float longitude;
    private ArrayList<AcessibilityVO> acessibilities;
    private ArrayList<String> types;
}
