package com.acessoatodos.places;

import com.acessoatodos.IntegrationTestServer;
import com.acessoatodos.aws.DynamoDbModule;
import com.acessoatodos.aws.EmbeddedDynamoDb;
import org.jooby.Jooby;
import org.jooby.test.Client;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class PlacesTest {
    @ClassRule
    public static EmbeddedDynamoDb db = new EmbeddedDynamoDb();

    @ClassRule
    public static IntegrationTestServer server = new IntegrationTestServer(
            new Jooby(){
                {
                    use(new PlacesModule(this));
                    use(DynamoDbModule.forTests(db.getAddress()));
                }
            });

    @ClassRule
    public static Client client = new Client(server.getAddress());

    @Test
    public void createTableTest() throws Exception {
        client.put("/placestable").expect(200);
        client.get("/placestable").expect(200);
        // TODO(danielfireman): Do better checking.
    }
}
