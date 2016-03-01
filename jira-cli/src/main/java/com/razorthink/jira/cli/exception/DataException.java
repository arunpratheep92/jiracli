package com.razorthink.jira.cli.exception;

@SuppressWarnings({ "serial", "unused" })
public class DataException extends RuntimeException {

	private final String errorCode;
	private final String errorMessage;

	public DataException(String errorCode, String errorMessage) {
		super(errorMessage);
		this.errorMessage = errorMessage;
		this.errorCode = errorCode;

	}
}
