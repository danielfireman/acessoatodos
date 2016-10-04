package com.acessoatodos.places;

import com.acessoatodos.IntegrationTestServer;
import com.google.maps.PendingResult;
import com.google.maps.model.PlaceDetails;
import org.jooby.Jooby;
import org.jooby.Status;
import org.jooby.json.Jackson;
import org.jooby.test.Client;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

public class PlacesTest {
    final static String CONTENT_TYPE = "application/json;charset=UTF-8";
    static GooglePlaces placesService = Mockito.mock(GooglePlaces.class);

    @ClassRule
    public static IntegrationTestServer server = new IntegrationTestServer(
            new Jooby() {
                {
                    use(new Jackson());
                    use(new PlacesModule(this));
                    use(GooglePlacesModule.forTests(placesService));
                }
            });

    @ClassRule
    public static Client client = new Client(server.getAddress());

    @Before
    public void createPlacesTable() throws Exception {
    }

    @After
    public void deletePlacesTable() throws Exception {
    }

    @Test
    public void putGet() throws Exception {
        String placeId = "1";
        String accessibilities = "[100]";

        PendingResult<PlaceDetails> pr = (PendingResult<PlaceDetails>) Mockito.mock(PendingResult.class);
        Mockito.when(pr.await()).thenReturn(new PlaceDetails());
        Mockito.when(placesService.placeDetails(placeId)).thenReturn(pr);

        Mockito.verify(placesService).placeDetails(placeId);
    }
    // TODO(danielfireman): Add tests for neaby search.
}
