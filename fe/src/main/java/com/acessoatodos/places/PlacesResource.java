package com.acessoatodos.places;

import java.util.List;

import javax.inject.Named;

import org.jooby.mvc.Consumes;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;
import org.jooby.mvc.Produces;

import com.acessoatodos.response.AcessoATodosResponse;

/**
 * This class is specific to define routes of resource
 */
@Path("/places")
@Consumes("json")
@Produces("json")
public class PlacesResource {
	
	/**
	 * Method used to retrieve a list of places
	 *
	 * @param latitude
	 *            latitude used by retrive places around.
	 * @param longitude
	 *            longitude used by retrive places around.
	 *
	 * @return List<PlaceVO>
	 */
	@GET
	public AcessoATodosResponse<List<PlaceVO>> get(
			@Named("latitude") Float latitude,
			@Named("longitude") Float longitude,
			PlacesController controller) {
		// TODO(heiner): Revisit Exception strategy.
		return new AcessoATodosResponse<List<PlaceVO>>(
				false, controller.getNearbyPlaces(latitude, longitude));
	}

}
