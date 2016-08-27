package br.com.acessoatodos.place;

import br.com.acessoatodos.response.AcessoATodosDefaultResponseVO;
import org.jooby.mvc.Consumes;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;
import org.jooby.mvc.Produces;

import javax.inject.Named;

/**
 * This class is specific to define routes of resource
 */
@Path("/places")
@Consumes("json")
@Produces("json")
public class PlaceResource {

    @GET
    /**
     * Method used to retrieve a list of places
     *
     * @param latitude  contact id used by retrive
     * @param longitude contact id used by retrive
     *
     * @return List<PlaceVO>
     */
    public AcessoATodosDefaultResponseVO retrieve(@Named("latitude") Float latitude, @Named("longitude") Float longitude) {
        PlaceModel placeModel = new PlaceModel();

        placeModel.setLatitude(latitude);
        placeModel.setLongitude(longitude);

        return new AcessoATodosDefaultResponseVO(false, placeModel.getNearbyPlaces());
    }
}
