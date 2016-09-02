package com.acessoatodos.places;

import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.DELETE;
import org.jooby.mvc.PUT;
import org.jooby.mvc.Path;
import org.jooby.mvc.Produces;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.google.inject.Inject;

/**
 * This class is specific to define routes of resource
 */
@Path("/placestable")
@Produces("json")
class PlacesTableResource {
	private DynamoDBMapper mapper;
	private DynamoDB db;

	@Inject
	PlacesTableResource(DynamoDB db, DynamoDBMapper mapper) {
		this.db = db;
		this.mapper = mapper;
	}

	// Creates places table.
	@PUT
	public Result put() throws InterruptedException {
		CreateTableRequest req = mapper.generateCreateTableRequest(PlacesTableModel.class);
		req.setProvisionedThroughput(new ProvisionedThroughput(2L,2L));
		Table table = db.createTable(req);
		table.waitForActive();
		return Results.ok(table.toString());
	}
	
	@DELETE
	public Result delete() throws InterruptedException {
		Table table = db.getTable(PlacesTableModel.TABLE_NAME);
		DeleteTableResult res = table.delete();
		table.waitForDelete();
		return Results.ok(res);
	}
}