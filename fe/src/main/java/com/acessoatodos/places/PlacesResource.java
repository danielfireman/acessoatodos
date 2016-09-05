package com.acessoatodos.places;

import com.google.inject.Inject;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.*;

import java.util.Set;

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
     * @return List<PlaceVO>
     */
    @GET
    public Result get(Float latitude, Float longitude) {
        // TODO(heiner): Revisit Exception strategy.
        // TODO(heiner): Check for errors in response.
        return Results.json(controller.getNearbyPlaces(latitude, longitude));
    }

    @PUT
    @Path("/:placeId")
    public Result insertUpdate(String placeId, @Body Set<Integer> acessibilities) {
        return Results.json(controller.insertOrUpdatePlace(placeId, acessibilities));
    }

}
