package com.acessoatodos.places;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Describes the places table.
 */
@Getter
@Setter
@DynamoDBTable(tableName = "places")
public class PlaceTableModel {
    static final String TABLE_NAME = "places";

    @DynamoDBHashKey
    String placeId;

    @DynamoDBAttribute
    Set<Integer> acessibilities;
}
