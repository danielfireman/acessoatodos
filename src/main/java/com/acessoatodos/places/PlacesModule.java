package com.acessoatodos.places;

import com.google.inject.Binder;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

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
        app.use(new GooglePlacesModule());
        app.use(PlacesResource.class);
        app.use(PlacesTableResource.class);
    }
}
