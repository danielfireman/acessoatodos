package com.acessoatodos.places;

import com.amazonaws.services.dynamodbv2.model.TableDescription;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.*;

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
public class PlacesTableResource {
    private static final long READ_CAPACITY_UNITS = 2L;
    private static final long WRITE_CAPACITY_UNITS = 2L;
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
        req.setProvisionedThroughput(new ProvisionedThroughput(READ_CAPACITY_UNITS, WRITE_CAPACITY_UNITS));
        Table table = db.createTable(req);
        table.waitForActive();
        return Results.ok(table.toString());
    }

    @DELETE
    public Result delete() throws InterruptedException {
        Table table = db.getTable(PlacesTableModel.PLACES_TABLE_NAME);
        DeleteTableResult res = table.delete();
        table.waitForDelete();
        return Results.ok(res);
    }

    @GET
    public Result get() {
        TableDescription tableDesc = db.getTable(PlacesTableModel.PLACES_TABLE_NAME).describe();
        return Results.json(tableDesc);
    }
}
