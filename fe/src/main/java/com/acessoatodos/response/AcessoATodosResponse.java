package com.acessoatodos.response;

import java.util.ArrayList;

import com.acessoatodos.exception.AcessoATodosException;

public class AcessoATodosResponse<T> {
	/*
	 * This field say if response is a error or not
	 */
	public Boolean hasError;

	/**
	 * Message list of results to interacting with the API
	 */
	public ArrayList<String> messages;

	/**
	 * Any data is returned in this attribute
	 */
	public T data;

	/**
	 * List of errors throw by system
	 */
	public ArrayList<AcessoATodosException> errors;

	public AcessoATodosResponse(boolean hasError, T data) {
		this.hasError = hasError;
		this.data = data;
	}
}
