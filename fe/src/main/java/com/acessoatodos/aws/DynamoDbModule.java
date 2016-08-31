package com.acessoatodos.aws;

import org.jooby.Env;
import org.jooby.Jooby;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.google.inject.Binder;
import com.typesafe.config.Config;

/**
 * Exports Amazon DynamoDB related entities.
 * 
 * <ul>
 * <li>com.amazonaws.services.dynamodbv2.document.DynamoDB
 * <li>com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
 * </ul>
 */
class DynamoDbModule implements Jooby.Module {
	private static String DYNAMODB_USE_LOCAL_PROP = "dynamodb.useLocal";
	private static String DYNAMODB_LOCAL_ADDR_PROP = "dynamodb.localAddr";

	@Override
	public void configure(final Env env, final Config conf, final Binder binder) {
		if (conf.getBoolean(DYNAMODB_USE_LOCAL_PROP)) {
			if (env.name() != "dev") {
				throw new RuntimeException(
						"One can only access local dynamo in dev environment. Forgot to set " + DYNAMODB_USE_LOCAL_PROP);
			}
			AmazonDynamoDBClient client = new AmazonDynamoDBClient(new EmptyAwsCredentials())
					.withEndpoint(conf.getString(DYNAMODB_LOCAL_ADDR_PROP));
			binder.bind(DynamoDB.class).toInstance(new DynamoDB(client));
			binder.bind(DynamoDBMapper.class).toInstance(new DynamoDBMapper(client));
		}
		// TODO(danielfireman): Complete to what to do in prod.
	}
}
