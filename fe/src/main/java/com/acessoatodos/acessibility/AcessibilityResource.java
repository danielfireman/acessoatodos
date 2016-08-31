/**
 * This copy of Woodstox XML processor is licensed under the
 * Apache (Software) License, version 2.0 ("the License").
 * See the License for details about distribution rights, and the
 * specific rights regarding derivate works.
 *
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing Woodstox, in file "ASL2.0", under the same directory
 * as this file.
 */
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
