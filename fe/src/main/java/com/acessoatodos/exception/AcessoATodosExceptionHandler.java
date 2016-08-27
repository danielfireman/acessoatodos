package com.acessoatodos.exception;

import org.jooby.Err;
import org.jooby.Request;
import org.jooby.Response;

/**
 * This class is used by catch a default exception and make the default response error
 */
public class AcessoATodosExceptionHandler implements Err.Handler {

    @Override
    public void handle(Request request, Response response, Err err) throws Throwable {
    }
}
