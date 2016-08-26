package br.com.acessoatodos.place.external.model;

import br.com.acessoatodos.place.PlaceVO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.jooby.Err;
import org.jooby.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GooglePlaceResponseModel {
    private ArrayList<String> html_attributions;
    private ArrayList<GooglePlaceModel> results;
    private String status;
    private String next_page_token;

    public GooglePlaceResponseModel hydrate(String message) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        GooglePlaceResponseModel resultGoogle = null;

        try {
            resultGoogle = mapper.readValue(message, GooglePlaceResponseModel.class);
        } catch (IOException e) {
            throw new Err(Status.UNPROCESSABLE_ENTITY, "Erro na conversão dos dados do google places.");
        }

        return resultGoogle;
    }

    public List<PlaceVO> convertToVO() {
        ArrayList<PlaceVO> places = new ArrayList<>();

        if (this.getResults() != null) {
            for (GooglePlaceModel googlePlaceModel : this.getResults()) {
                PlaceVO placeVO = new PlaceVO();

                placeVO.setPlaceId(googlePlaceModel.getPlace_id());
                placeVO.setName(googlePlaceModel.getName());
                placeVO.setLatitude(googlePlaceModel.getGeometry().getLocation().getLat());
                placeVO.setLongitude(googlePlaceModel.getGeometry().getLocation().getLng());
                placeVO.setTypes(googlePlaceModel.getTypes());

                places.add(placeVO);
            }
        }

        return places;
    }

}
