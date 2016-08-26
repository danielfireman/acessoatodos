package br.com.acessoatodos.response;

import br.com.acessoatodos.exception.AcessoATodosException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * This class is a view object default of all the responses of system
 */
@Getter
@Setter
public class AcessoATodosDefaultResponseVO {
    /**
     * This field say if response is a error or not
     */
    private Boolean hasError;

    /**
     * Message list of results to interacting with the API
     */
    private ArrayList<String> messages;

    /**
     * Any data is returned in this attribute
     */
    private Object data;

    /**
     * List of errors throw by system
     */
    private ArrayList<AcessoATodosException> errors;

    public AcessoATodosDefaultResponseVO(Boolean hasError, Object data) {
        this.hasError = hasError;
        this.data = data;
    }

}
