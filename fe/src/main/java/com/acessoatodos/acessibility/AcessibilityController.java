package com.acessoatodos.acessibility;

import com.acessoatodos.places.PlaceDynamoModel;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.google.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This class is responsible to interact with external API, database and return view object.
 */
class AcessibilityController {
    static AmazonDynamoDBClient client = new AmazonDynamoDBClient(new ProfileCredentialsProvider());

    PlaceDynamoModel insertUpdatePlace(String placeId, Set<Integer> acessibilities) {
        client.withRegion(Regions.SA_EAST_1);
        client.withEndpoint("http://localhost:8000");

        DynamoDBMapper mapper = new DynamoDBMapper(client);

        final CreateTableRequest createTableRequest= mapper.generateCreateTableRequest(PlaceDynamoModel.class);
        createTableRequest.setTableName(PlaceDynamoModel.TABLE_NAME_ACCESIBILITIES_OF_PLACES);
        createTableRequest.setProvisionedThroughput(new ProvisionedThroughput(2L,2L));

        client.createTable(createTableRequest);

        HashMap<String, AttributeValue> eav = new HashMap<String, AttributeValue>();

        eav.put(":v1", new AttributeValue().withS(placeId));

        DynamoDBQueryExpression<PlaceDynamoModel> queryExpression = new DynamoDBQueryExpression<PlaceDynamoModel>()
                .withIndexName("placeId")
                .withConsistentRead(false)
                .withKeyConditionExpression("placeId = :v1")
                .withExpressionAttributeValues(eav);

        List<PlaceDynamoModel> placeDynamoModelList = mapper.query(PlaceDynamoModel.class, queryExpression);

        PlaceDynamoModel placeDynamoModel;

        if (placeDynamoModelList.size() > 0) {
            placeDynamoModel = placeDynamoModelList.get(0);
            placeDynamoModel.setAcessibilities(acessibilities);
        } else {
            placeDynamoModel = new PlaceDynamoModel();
            placeDynamoModel.setPlaceId(placeId);
            placeDynamoModel.setAcessibilities(acessibilities);
        }

        mapper.save(placeDynamoModel);

        return placeDynamoModel;
    }
}
