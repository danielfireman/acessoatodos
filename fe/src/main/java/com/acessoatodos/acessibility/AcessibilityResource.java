package com.acessoatodos.acessibility;

import com.google.inject.Inject;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.*;

import java.util.Set;

/**
 * Created by NoteSamsung on 30/08/2016.
 */
@Path("/acessibility")
@Consumes("json")
@Produces("json")
public class AcessibilityResource {
    private AcessibilityController controller;

    @Inject
    AcessibilityResource(AcessibilityController controller) {
        this.controller = controller;
    }

    @PUT
    @Path("/{placeId}")
    public Result insert(String placeId, @Body Set<Integer> acessibilities) {
        return Results.json(controller.insertUpdatePlace(placeId, acessibilities));
    }
}
