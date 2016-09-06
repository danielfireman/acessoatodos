package com.acessoatodos.aws;

import com.acessoatodos.AvailablePort;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import org.junit.rules.ExternalResource;

public class EmbeddedDynamoDb extends ExternalResource {
    private final AvailablePort port;
    private DynamoDBProxyServer server;

    public EmbeddedDynamoDb() {
        // This one should be copied during test-compile time. If project's basedir does not contains a folder
        // named 'native-libs' please try '$ mvn clean install' from command line first
        System.setProperty("sqlite4java.library.path", "native-libs");
        this.port = new AvailablePort();
    }

    @Override
    public void before() throws Throwable {
        try {
            this.server = ServerRunner.createServerFromCommandLineArgs(
                    new String[]{"-inMemory", "-port", port.getPort()}
            );
            this.server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void after() {
        try {
            if (null != this.server) {
                server.stop();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.port.release();
    }

    public String getAddress() {
        return String.format("http://localhost:%s", this.port.getPort());
    }
}
