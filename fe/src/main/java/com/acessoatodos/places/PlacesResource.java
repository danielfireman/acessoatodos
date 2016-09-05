package com.acessoatodos.places;

import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.*;

import com.google.inject.Inject;

import java.util.List;

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
     * @param lat latitude used by retrive places around.
     * @param lng longitude used by retrive places around.
     * @return List<PlaceVO>
     */
    @GET
    public Result get(Float lat, Float lng) {
        // TODO(heiner): Revisit Exception strategy.
        // TODO(heiner): Check for errors in response.
        return Results.json(controller.getNearbyPlaces(lat, lng));
    }

}
