package com.acessoatodos;

import java.io.IOException;
import java.net.ServerSocket;

import org.jooby.Jooby;
import org.junit.rules.ExternalResource;

public final class IntegrationTestServer extends ExternalResource {
    private Jooby app;
    private AvailablePort port;

    public IntegrationTestServer(final Jooby app) {
        this.app = app;
        this.port = new AvailablePort();
    }

    @Override
    protected void before() throws Throwable {
        // NOTE(danielfireman): Somehow setting application.port through Module usage (config)
        // was not working.
        System.setProperty("application.port", this.port.getPort());
        System.setProperty("server.join", "false");
        System.setProperty("application.env", "test");
        this.app.start();
    }

    @Override
    protected void after() {
        this.app.stop();
        this.port.release();
    }

    public String getAddress() {
        return String.format("http://localhost:%s", this.port.getPort());
    }
}
