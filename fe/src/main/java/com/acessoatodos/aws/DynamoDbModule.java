package com.acessoatodos.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import org.apache.commons.cli.ParseException;
import org.jooby.Env;
import org.jooby.Jooby;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.kms.model.UnsupportedOperationException;
import com.google.inject.Binder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Exports Amazon DynamoDB related entities.
 *
 * com.amazonaws.services.dynamodbv2.document.DynamoDB
 * com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
 *
 */
class DynamoDbModule implements Jooby.Module {
    private static String DYNAMODB_USE_LOCAL_PROP = "dynamodb.useLocal";
    private static String DYNAMODB_LOCAL_ADDR_PROP = "dynamodb.localAddr";

    @Override
    public void configure(final Env env, final Config conf, final Binder binder) {
        AmazonDynamoDBClient client = null;
        switch (env.name()) {
            case "test":
                // Heavily inspired in: http://stackoverflow.com/questions/26901613/easier-dynamodb-local-testing
                // This one should be copied during test-compile time. If project's basedir does not contains a folder
                // named 'native-libs' please try '$ mvn clean install' from command line first
                System.setProperty("sqlite4java.library.path", "native-libs");

                // Create an in-memory and in-process instance of DynamoDB Local that skips HTTP
                DynamoDBEmbedded.create();
                System.out.print("Embedded DynamoDB created. Starting HTTP proxy server.");
                try (ServerSocket dynamoDbSocket = getFreeServerSocket()) {
                    String port = Integer.toString(dynamoDbSocket.getLocalPort());
                    String[] args = new String[]{"-inMemory", "-port", port};
                    DynamoDBProxyServer dbProxyServer = null;
                    try {
                        // Proving proxy server to feature.
                        dbProxyServer = ServerRunner.createServerFromCommandLineArgs(args);
                        binder.bind(DynamoDBProxyServer.class).toInstance(dbProxyServer);

                        // TODO(danielfireman): Create ExternalResource.
                        client = new AmazonDynamoDBClient(new EmptyAwsCredentials());
                        client.withEndpoint("http://localhost:" + port);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        if (dbProxyServer != null) {
                            try {
                                dbProxyServer.stop();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                } catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
                System.out.print("DynamoDB HTTP proxy server up and running.");
                break;
            case "dev":
                boolean local = false;
                // Optional field. If not set, use local.
                // TODO(danielfireman): Is there a better way to do this?
                try {
                    local = conf.getBoolean(DYNAMODB_USE_LOCAL_PROP);
                } catch (ConfigException.Missing couldHappen) {
                }
                if (local) {
                    client = new AmazonDynamoDBClient(new EmptyAwsCredentials());
                    client.withEndpoint(conf.getString(DYNAMODB_LOCAL_ADDR_PROP));
                } else {
                    client = new AmazonDynamoDBClient();
                }
                break;
            default:
                throw new UnsupportedOperationException("Application environment not supported: " + env.name());
        }
        binder.bind(DynamoDB.class).toInstance(new DynamoDB(client));
        binder.bind(DynamoDBMapper.class).toInstance(new DynamoDBMapper(client));
    }

    private ServerSocket getFreeServerSocket() {
        for (int i = 0; i < 3; i++) {
            try (ServerSocket socket = new ServerSocket(0)) {
                return socket;
            } catch (IOException expected) {}
        }
        throw new RuntimeException("Reached maximum number of attempts to get free port. Bailing out...");
    }
}
