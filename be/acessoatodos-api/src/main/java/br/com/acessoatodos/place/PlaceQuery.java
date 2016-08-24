package br.com.acessoatodos.place;

/**
 * Created by k-heiner@hotmail.com on 22/08/2016.
 */
public abstract class PlaceQuery {

//    protected static List<PlaceVO> getNearbyPlaces() {
//        AmazonDynamoDBClient amazonDynamoDBClient = new AmazonDynamoDBClient();
//
////        amazonDynamoDBClient.withRegion(Regions.US_EAST_1);
//        amazonDynamoDBClient.withEndpoint("http://localhost:8000");
//
//        ArrayList<PlaceVO> places = new ArrayList<>();
//
//        PlaceVO placeVO = new PlaceVO();
//
//        return places;
//    }
//
//    protected static void createPlaceTable() throws InterruptedException {
//        DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new ProfileCredentialsProvider()));
//
//        ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
//        attributeDefinitions.add(new AttributeDefinition().withAttributeName("id").withAttributeType("Number"));
//        attributeDefinitions.add(new AttributeDefinition().withAttributeName("placeId").withAttributeType("String"));
//        attributeDefinitions.add(new AttributeDefinition().withAttributeName("acessibilities").withAttributeType("List"));
//
//        ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
//        keySchema.add(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH));
//        keySchema.add(new KeySchemaElement().withAttributeName("placeId").withKeyType(KeyType.RANGE));
//        keySchema.add(new KeySchemaElement().withAttributeName("acessibilities"));
//
//        CreateTableRequest request = new CreateTableRequest()
//                .withTableName("acessibilitiesOfPlaces")
//                .withKeySchema(keySchema)
//                .withAttributeDefinitions(attributeDefinitions)
//                .withProvisionedThroughput(
//                        new ProvisionedThroughput().withReadCapacityUnits(2L).withWriteCapacityUnits(2L)
//                );
//
//        Table table = dynamoDB.createTable(request);
//
//        table.waitForActive();
//    }
}
