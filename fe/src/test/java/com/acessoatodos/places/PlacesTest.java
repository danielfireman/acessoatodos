package com.acessoatodos.places;

import com.acessoatodos.IntegrationTestServer;
import com.acessoatodos.aws.DynamoDbModule;
import com.acessoatodos.aws.EmbeddedDynamoDb;
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
    public static EmbeddedDynamoDb db = new EmbeddedDynamoDb();

    @ClassRule
    public static IntegrationTestServer server = new IntegrationTestServer(
            new Jooby() {
                {
                    use(new Jackson());
                    use(new PlacesModule(this));
                    use(DynamoDbModule.forTests(db.getAddress()));
                    use(GooglePlacesModule.forTests(placesService));
                }
            });

    @ClassRule
    public static Client client = new Client(server.getAddress());

    @Before
    public void createPlacesTable() throws Exception {
        client.put("/placestable").expect(Status.OK.value());
    }

    @After
    public void deletePlacesTable() throws Exception {
        client.delete("/placestable").expect(Status.OK.value());
    }

    @Test
    public void putGet() throws Exception {
        String placeId = "1";
        String accessibilities = "[100]";
        client.put("/places/" + placeId)
                .body(String.format("{\"accessibilities\":%s}", accessibilities), CONTENT_TYPE)
                .expect(Status.OK.value());

        PendingResult<PlaceDetails> pr = (PendingResult<PlaceDetails>) Mockito.mock(PendingResult.class);
        Mockito.when(pr.await()).thenReturn(new PlaceDetails());
        Mockito.when(placesService.placeDetails(placeId)).thenReturn(pr);

        client.get("/places/1")
                .expect(Status.OK.value())
                .expect(String.format(
                        "{\"placeId\":\"%s\",\"accessibilities\":%s}", placeId, accessibilities));

        Mockito.verify(placesService).placeDetails(placeId);
    }
    // TODO(danielfireman): Add tests for neaby search.
}
