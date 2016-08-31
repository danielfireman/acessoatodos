package com.acessoatodos.places;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.*;

import com.acessoatodos.response.AcessoATodosResponse;
import com.google.inject.Inject;

/**
 * This class is specific to define routes of resource
 */
@Path("/places")
@Consumes("json")
@Produces("json")
public class PlacesResource {
    private PlacesController controller;

    @Inject
    PlacesResource(PlacesController controller) {
        this.controller = controller;
    }

    /**
     * Method used to retrieve a list of places
     *
     * @param latitude  latitude used by retrive places around.
     * @param longitude longitude used by retrive places around.
     *
     * @return List<PlaceVO>
     */
    @GET
    public Result get(Float latitude, Float longitude) {
        // TODO(heiner): Revisit Exception strategy.
        // TODO(heiner): Check for errors in response.
        List<PlaceVO> nearbyPlaces = controller.getNearbyPlaces(latitude, longitude);
        return Results.json(new AcessoATodosResponse<List<PlaceVO>>(false, nearbyPlaces));
    }

}
