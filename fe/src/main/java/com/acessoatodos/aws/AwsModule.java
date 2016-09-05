package com.acessoatodos.aws;

import org.jooby.Env;
import org.jooby.Jooby;

import com.google.inject.Binder;
import com.typesafe.config.Config;

/**
 * Module responsible for binding entities related to Amazon AWS.
 * Ideally, this should be the only public class in this package.
 */
public class AwsModule implements Jooby.Module {
    private Jooby app;

    public AwsModule(Jooby app) {
        this.app = app;
    }

    @Override
    public void configure(Env env, Config conf, Binder binder) {
        app.use(new DynamoDbModule());
    }
}
