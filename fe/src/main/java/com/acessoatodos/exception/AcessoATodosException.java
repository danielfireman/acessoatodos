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
