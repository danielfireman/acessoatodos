package com.acessoatodos.response;

import java.util.ArrayList;

import com.acessoatodos.exception.AcessoATodosException;

public class AcessoATodosResponse<T> {
	/*
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
	private T data;

	/**
	 * List of errors throw by system
	 */
	private ArrayList<AcessoATodosException> errors;

	public AcessoATodosResponse(boolean hasError, T data) {
		this.hasError = hasError;
		this.data = data;
	}
}
