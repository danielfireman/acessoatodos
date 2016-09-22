package com.acessoatodos.places;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import org.jooby.MediaType;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.*;

import java.util.List;
import java.util.Set;

/**
 * This class is specific to define routes of resource
 */
// TODO(danielfireman): Document this API.
@Path("/places")
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
     * @return List<Place>
     */
    @GET
    public Result get(Float latitude, Float longitude) {
        // TODO(danielfireman): Revisit Exception strategy.
        // TODO(danielfireman): Check for errors in response.
        return Results.json(controller.getNearbyPlaces(latitude, longitude));
    }

    // Parameter of the add upsert method.
    static class UpsertParam {
        @JsonProperty Set<Integer> accessibilities;
    }

    @PUT
    @Path("/:placeId")
    @Consumes("application/json")
    @Produces("application/json")
    public Result upsertPlace(String placeId, @Body UpsertParam param) {
        return Results.ok(controller.upsert(placeId, param.accessibilities));
    }

    @GET
    @Path("/:placeId")
    @Produces("application/json")
    public Result getPlace(String placeId) {
        return Results.ok(controller.get(placeId));
    }
}
