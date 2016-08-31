/**
 * This copy of Woodstox XML processor is licensed under the
 * Apache (Software) License, version 2.0 ("the License").
 * See the License for details about distribution rights, and the
 * specific rights regarding derivate works.
 *
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing Woodstox, in file "ASL2.0", under the same directory
 * as this file.
 */
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
