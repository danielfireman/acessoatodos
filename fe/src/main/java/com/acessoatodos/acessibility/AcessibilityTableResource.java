package com.acessoatodos.acessibility;

import java.util.Set;

import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.Consumes;
import org.jooby.mvc.PUT;
import org.jooby.mvc.Path;
import org.jooby.mvc.Produces;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.google.inject.Inject;

import lombok.Getter;
import lombok.Setter;

/**
 * This class is specific to define routes of resource
 */
@Path("/acessibilitytable")
@Consumes("json")
@Produces("json")
public class AcessibilityTableResource {
	public static final String TABLE_NAME_ACCESIBILITIES_OF_PLACES = "acessibilitiesOfPlaces";

	@Getter
	@Setter
	@DynamoDBTable(tableName=TABLE_NAME_ACCESIBILITIES_OF_PLACES)
	static class AcessibilityItemModel {
		@DynamoDBAutoGeneratedKey
		@DynamoDBHashKey
		private String id;

		@DynamoDBAttribute
		private Set<Integer> acessibilities;

		@DynamoDBRangeKey
		private String placeId;
	}

	private AmazonDynamoDBClient client;
	@Inject
	AcessibilityTableResource(AmazonDynamoDBClient client) {
		this.client = client;
	}

	// Creates acessibility table.
	@PUT
	public Result put() {
		// TODO(danielfireman): Inject Mapper.
		DynamoDBMapper mapper = new DynamoDBMapper(client);
		CreateTableRequest req = mapper.generateCreateTableRequest(AcessibilityItemModel.class);
		req.setProvisionedThroughput(new ProvisionedThroughput(2L,2L));
		return Results.ok(client.createTable(req));
	}
}
