package br.com.acessoatodos.utils;

import lombok.Getter;
import lombok.Setter;
import org.jooby.Err;
import org.jooby.Status;
import org.jooby.internal.parser.StringConstructorParser;

/**
 * Created by k-heiner@hotmail.com on 23/08/2016.
 */
@Getter
@Setter
public class AcessoAaTodosException extends Exception {
    private Integer code;
    private String message;

    public AcessoAaTodosException(Status status, String message) {
        this.setCode(status.value());
        this.setMessage(message);
    }
}
