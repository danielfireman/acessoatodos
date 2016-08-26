package br.com.acessoatodos.exception;

import lombok.Getter;
import lombok.Setter;
import org.jooby.Err;
import org.jooby.Status;
import org.jooby.internal.parser.StringConstructorParser;

/**
 * This exception is a default exception used by system to throw an error
 */
@Getter
@Setter
public class AcessoATodosException extends Exception {
    private Integer code;
    private String message;

    public AcessoATodosException(Status status, String message) {
        this.setCode(status.value());
        this.setMessage(message);
    }
}
