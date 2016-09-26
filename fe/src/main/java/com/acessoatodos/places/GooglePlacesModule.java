package com.acessoatodos.places;


import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.util.Providers;
import com.google.maps.GeoApiContext;
import com.typesafe.config.Config;
import org.jooby.Env;
import org.jooby.Jooby;

class GooglePlacesModule implements Jooby.Module {
    public static Jooby.Module forTests(final GooglePlaces service) {
        return (Env env, Config conf, Binder binder) -> {
            binder.bind(GooglePlaces.class).toProvider(Providers.of(service));
        };
    }

    @Override
    public void configure(Env env, Config conf, Binder binder) {
        final String placesKey = System.getenv().get("KEY_GOOGLE_PLACES");
        if (placesKey.length() == 0) {
            throw new RuntimeException("KEY_GOOGLE_PLACES environment variable not set.");
        }

        binder.install(new Module() {
            @Override
            public void configure(Binder binder) {
            }

            @Provides
            GeoApiContext getContext() {
                return new GeoApiContext().setApiKey(placesKey);
            }
        });
    }
}
