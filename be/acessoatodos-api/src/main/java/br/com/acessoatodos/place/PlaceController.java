package br.com.acessoatodos.place;

import br.com.acessoatodos.place.external.model.GooglePlaceModel;
import br.com.acessoatodos.place.external.model.GooglePlaceResponseModel;
import br.com.acessoatodos.utils.AcessoAaTodosException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooby.Err;
import org.jooby.Status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by k-heiner@hotmail.com on 22/08/2016.
 */
public class PlaceController {

    private final String KEY_GOOGLE_PLACES = "AIzaSyCJaq3D13uFou0jF9hJwMUQ2GfQH43ZuWk";
    private final Integer RADIUS_RANGE_1000M = 1000;

    public List<PlaceVO> getNearbyPlaces(Float latitude, Float longitude) {
        String result = requestToGooglePlaces(latitude, longitude);

        GooglePlaceResponseModel googlePlaceResponseModel = convertResult(result);

        List<PlaceVO> places = hydrateGooglePlace(googlePlaceResponseModel);

        return places;
    }

    private String requestToGooglePlaces(Float latitude, Float longitude) {
        String placeUrlToSearch =
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=" + latitude + "," + longitude +
                        "&radius=" + RADIUS_RANGE_1000M +
                        "&key=" + KEY_GOOGLE_PLACES;

        URL url = null;

        try {
            url = new URL(placeUrlToSearch);
        } catch (MalformedURLException e) {
            throw new Err(Status.BAD_REQUEST, "URL de requisição do google mal formada.");
        }

        String message = "";

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
        } catch (IOException e) {
            throw new Err(Status.SERVER_ERROR, "Erro na abertura da stream para o google places.");
        }

        try {
            for (String line; (line = reader.readLine()) != null; ) {
                message += line;
            }
        } catch (IOException e) {
            throw new Err(Status.SERVER_ERROR, "Erro na leitura da resposta do google places.");
        }

        return message;
    }

    private GooglePlaceResponseModel convertResult(String message) {
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

    private List<PlaceVO> hydrateGooglePlace(GooglePlaceResponseModel googlePlaceResponseModel) {
        ArrayList<PlaceVO> places = new ArrayList<>();

        if (googlePlaceResponseModel.getResults() != null) {
            for (GooglePlaceModel googlePlaceModel : googlePlaceResponseModel.getResults()) {
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
