package br.com.acessoatodos.place;

import br.com.acessoatodos.place.external.model.GooglePlaceResponseModel;
import org.jooby.Err;
import org.jooby.Status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * This class is responsible to interact with external API, database
 * and hydrate the object to return to view
 */
class PlaceController {

    /**
     * Key of API registered on google
     */
    private final String KEY_GOOGLE_PLACES = System.getenv().get("KEY_GOOGLE_PLACES");

    /**
     * Distance to define the radius range to search on google places API
     */
    private final Integer RADIUS_RANGE_1000_METERS = 1000;

    public List<PlaceVO> getNearbyPlaces(Float latitude, Float longitude) {
        String result = requestToGooglePlaces(latitude, longitude);

        GooglePlaceResponseModel googlePlaceResponseModel = new GooglePlaceResponseModel();
        googlePlaceResponseModel = googlePlaceResponseModel.hydrate(result);

        List<PlaceVO> places = googlePlaceResponseModel.convertToVO();

        return places;
    }

    private String requestToGooglePlaces(Float latitude, Float longitude) {
        String placeUrlToSearch =
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=" + latitude + "," + longitude +
                        "&radius=" + RADIUS_RANGE_1000_METERS +
                        "&key=" + KEY_GOOGLE_PLACES;

        System.out.println(KEY_GOOGLE_PLACES);

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

}
