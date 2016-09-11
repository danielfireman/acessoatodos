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
@DynamoDBTable(tableName = PlaceTableModel.PLACES_TABLE_NAME)
public class PlaceTableModel {
    static final String PLACES_TABLE_NAME = "PlaceTableModel";

    @DynamoDBHashKey
    String placeId;

    @DynamoDBAttribute
    Set<Integer> acessibilities;
}
