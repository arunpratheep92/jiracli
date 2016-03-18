package com.razorthink.jira.cli.utils;

public class Response {

	private Object object;
	private String errorCode;
	private String errorMessage;

	public Object getObject()
	{
		return object;
	}

	public void setObject( Object object )
	{
		this.object = object;
	}

	public String getErrorCode()
	{
		return errorCode;
	}

	public void setErrorCode( String errorCode )
	{
		this.errorCode = errorCode;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage( String errorMessage )
	{
		this.errorMessage = errorMessage;
	}

}
