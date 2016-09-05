package com.acessoatodos;

import org.jooby.test.Client;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class AppTest {
    @ClassRule
    public static IntegrationTestServer app = new IntegrationTestServer(new App());

    @Rule
    public Client server = new Client(app.getAdress());

    @Test
    public void ping() throws Exception {
        server.get("/ping").expect(200);
    }
}
