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

import org.jooby.Status;

import lombok.Getter;
import lombok.Setter;

/**
 * This exception is a default exception used by system to throw an error
 */
@Getter
@Setter
public class AcessoATodosException extends Exception {
	private Integer code;
    private String message;

    public AcessoATodosException(Status status, String message) {
    	this.code = status.value();
    	this.message = message;
    }
}
