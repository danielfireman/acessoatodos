package com.acessoatodos.places;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.google.inject.Binder;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;
import org.jooby.json.Jackson;

/**
 * Module responsible for binding routes and resources related to places.
 * Ideally, this should be the only public class in this package.
 */
public class PlacesModule implements Jooby.Module {
    private Jooby app;

    public PlacesModule(Jooby app) {
        this.app = app;
    }

    @Override
    public void configure(Env env, Config conf, Binder binder) {
        binder.requestInjection(DynamoDB.class);
        binder.requestInjection(DynamoDBMapper.class);

        app.use(new GooglePlacesModule());
        app.use(PlacesResource.class);
        app.use(PlacesTableResource.class);
    }
}
