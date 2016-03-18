package com.razorthink.jira.cli.domain;

public class TimesheetReport {

	private String project;
	private String key;
	private String sprint;
	private String type;
	private String title;
	private String date;
	private String username;
	private Integer timeSpent;
	private String comment;

	public String getProject()
	{
		return project;
	}

	public void setProject( String project )
	{
		this.project = project;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey( String key )
	{
		this.key = key;
	}

	public String getSprint()
	{
		return sprint;
	}

	public void setSprint( String sprint )
	{
		this.sprint = sprint;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String type )
	{
		this.type = type;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle( String title )
	{
		this.title = title;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate( String date )
	{
		this.date = date;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername( String username )
	{
		this.username = username;
	}

	public Integer getTimeSpent()
	{
		return timeSpent;
	}

	public void setTimeSpent( Integer timeSpent )
	{
		this.timeSpent = timeSpent;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}

}
