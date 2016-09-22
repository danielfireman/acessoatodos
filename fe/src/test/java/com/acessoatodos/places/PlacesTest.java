package com.acessoatodos.places;

import com.acessoatodos.IntegrationTestServer;
import com.acessoatodos.aws.DynamoDbModule;
import com.acessoatodos.aws.EmbeddedDynamoDb;
import org.jooby.Jooby;
import org.jooby.Status;
import org.jooby.json.Jackson;
import org.jooby.test.Client;
import org.junit.*;

public class PlacesTest {
    @ClassRule
    public static EmbeddedDynamoDb db = new EmbeddedDynamoDb();

    @ClassRule
    public static IntegrationTestServer server = new IntegrationTestServer(
            new Jooby() {
                {
                    use(new Jackson());
                    use(new PlacesModule(this));
                    use(DynamoDbModule.forTests(db.getAddress()));
                }
            });

    @ClassRule
    public static Client client = new Client(server.getAddress());

    final static String CONTENT_TYPE = "application/json;charset=UTF-8";

    @Before
    public  void createPlacesTable() throws Exception {
        client.put("/placestable").expect(Status.OK.value());
    }
    @After
    public  void deletePlacesTable() throws Exception {
        client.delete("/placestable").expect(Status.OK.value());
    }

    @Test
    public void putGet() throws Exception {
        client.put("/places/1")
                .body("{\"accessibilities\":[100]}", CONTENT_TYPE)
                .expect(Status.OK.value());
        client.get("/places/1")
                .expect(Status.OK.value())
                .expect("{\"placeId\":\"1\",\"accessibilities\":[100]}");
    }

    // TODO(danielfireman): Add tests for neaby search.
}
