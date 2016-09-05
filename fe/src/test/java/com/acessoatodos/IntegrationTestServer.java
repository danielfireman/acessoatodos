package com.acessoatodos;

import java.io.IOException;
import java.net.ServerSocket;

import org.jooby.Jooby;
import org.junit.rules.ExternalResource;

public class IntegrationTestServer extends ExternalResource {

    private Jooby app;
    // ServerSocket is pre-fetching a free port to use in tests. Must be closed, though.
    private ServerSocket socket;
    private int port;

    public IntegrationTestServer(final Jooby app) {
        this.app = app;

        // A little bit of resiliency: try a few times before bail out.
        for (int i = 0; i < 3; i++) {
            try (ServerSocket socket = new ServerSocket(0)) {
                this.socket = socket;
                this.port = socket.getLocalPort();
                break;
            } catch (IOException ignored) {
                this.socket = null;
            }
        }
    }

    @Override
    protected void before() throws Throwable {
        if (this.socket == null) {
            throw new IOException("Couldn't find free port to run tests.");
        }
        // NOTE(danielfireman): Somehow setting application.port through Module usage (config)
        // was not working.
        System.setProperty("application.port", Integer.toString(this.port));
        System.setProperty("server.join", "false");
        System.setProperty("application.env", "test");
        try {
            this.socket.close();
        } catch (IOException ignored) {
        }
        this.app.start();
    }

    @Override
    protected void after() {
        this.app.stop();
    }

    public String getAdress() {
        return String.format("http://localhost:%d", port);
    }
}
