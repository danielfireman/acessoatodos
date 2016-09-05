package com.acessoatodos.places;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.jooby.Err;
import org.jooby.Status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

class GooglePlaces {
    /**
     * Key of API registered at google
     */
    // TODO(heiner): Add a check that breaks the application if this key is not set.
    private static final String KEY_GOOGLE_PLACES = System.getenv().get("KEY_GOOGLE_PLACES");

    /**
     * Distance to define the radius range to search on google places API. In meters.
     */
    private final Integer RADIUS_RANGE_1000_METERS = 1000;

    private ObjectMapper mapper;

    @Inject
    public GooglePlaces(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    GooglePlacesResponse nearbySearch(float latitude, float longitude) {
        String placeUrlToSearch =
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=" + latitude + "," + longitude +
                        "&radius=" + RADIUS_RANGE_1000_METERS +
                        "&key=" + KEY_GOOGLE_PLACES;

        URL url = null;
        try {
            url = new URL(placeUrlToSearch);
        } catch (MalformedURLException e) {
            throw new Err(Status.BAD_REQUEST, "URL de requisição do google mal formada.");
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
        } catch (IOException e) {
            throw new Err(Status.SERVER_ERROR, "Erro na abertura da stream para o google places.");
        }

        StringBuffer message = new StringBuffer();
        try {
            for (String line; (line = reader.readLine()) != null; ) {
                message.append(line);
            }
        } catch (IOException e) {
            throw new Err(Status.SERVER_ERROR, "Erro na leitura da resposta do google places.");
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // Print exception, but does not error request.
                e.printStackTrace();
            }
        }
        try {
            return mapper.readValue(message.toString(), GooglePlacesResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Err(Status.UNPROCESSABLE_ENTITY, "Erro na conversão dos dados do google places.");
        }
    }
}
