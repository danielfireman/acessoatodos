package com.acessoatodos.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.kms.model.UnsupportedOperationException;
import com.google.inject.Binder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.jooby.Env;
import org.jooby.Jooby;

/**
 * Exports Amazon DynamoDB related entities.
 *
 * com.amazonaws.services.dynamodbv2.document.DynamoDB
 * com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
 *
 */
public class DynamoDbModule implements Jooby.Module {
    private static String DYNAMODB_USE_LOCAL_PROP = "dynamodb.useLocal";
    private static String DYNAMODB_LOCAL_ADDR_PROP = "dynamodb.localAddr";

    public static Jooby.Module forTests(final String dbAddress) {
        return (Env env, Config conf, Binder binder) -> {
                AmazonDynamoDBClient client = new AmazonDynamoDBClient(new EmptyAwsCredentials());
                client.withEndpoint(dbAddress);
                binder.bind(DynamoDB.class).toInstance(new DynamoDB(client));
                binder.bind(DynamoDBMapper.class).toInstance(new DynamoDBMapper(client));
        };
    }

    @Override
    public void configure(final Env env, final Config conf, final Binder binder) {
        AmazonDynamoDBClient client = null;
        switch (env.name()) {
            case "dev":
                boolean local = false;
                // Optional field. If not set, use local.
                // TODO(danielfireman): Is there a better way to do this?
                try {
                    local = conf.getBoolean(DynamoDbModule.DYNAMODB_USE_LOCAL_PROP);
                } catch (ConfigException.Missing couldHappen) {
                }
                if (local) {
                    client = new AmazonDynamoDBClient(new EmptyAwsCredentials());
                    client.withEndpoint(conf.getString(DynamoDbModule.DYNAMODB_LOCAL_ADDR_PROP));
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
}
