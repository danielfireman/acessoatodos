package com.acessoatodos.places;

import com.google.inject.Inject;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.*;

/**
 * This class is specific to define routes of resource
 */
@Path("/placestable")
@Produces("application/json")
public class PlacesTableResource {
    private static final long READ_CAPACITY_UNITS = 2L;
    private static final long WRITE_CAPACITY_UNITS = 2L;

    // TODO(danielfireman): Inject mongodb stuff.
    @Inject
    PlacesTableResource() {
    }

    // Creates places table.
    @PUT
    public Result put() throws InterruptedException {
        // TODO(danielfireman): Create table.
        return Results.ok();
    }

    @DELETE
    public Result delete() throws InterruptedException {
        // TODO(danielfireman): Delete table.
        return Results.ok();
    }

    @GET
    public Result get() {
        // TODO(danielfireman): Get table description.
        return Results.json("");
    }
}
