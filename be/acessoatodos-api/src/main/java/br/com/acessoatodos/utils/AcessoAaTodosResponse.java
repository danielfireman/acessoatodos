package br.com.acessoatodos.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * Created by k-heiner@hotmail.com on 23/08/2016.
 */
@Getter
@Setter
public class AcessoAaTodosResponse {
    private Boolean hasError;
    private ArrayList<String> messages;
    private Object data;
    private ArrayList<AcessoAaTodosException> errors;

    public AcessoAaTodosResponse() {}

    public AcessoAaTodosResponse(Boolean hasError, Object data) {
        this.hasError = hasError;
        this.data = data;
    }

}
