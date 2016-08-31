package com.acessoatodos.dynamodb;

import org.jooby.Env;
import org.jooby.Jooby;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.typesafe.config.Config;

public class DynamoDbModule implements Jooby.Module {
	@Override
	public void configure(Env env, Config conf, Binder binder) {
		binder.install(new Module() {
			@Override
			public void configure(Binder binder) {}

			@Provides
			DynamoDB provideDynamoDB(AmazonDynamoDBClient client) {
				return new DynamoDB(client);
			}
			
			@Provides
			DynamoDBMapper provideDynamoDBMapper(AmazonDynamoDBClient client) {
				return new DynamoDBMapper(client);
			}
		});
	}
}
