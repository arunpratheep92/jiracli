package com.razorthink.jira.cli.domain;

import java.util.List;

public class AggregateUserReport {

	private List<UserReport> issues;
	private String sprintName;
	private String sprintStartDate;
	private String sprintEndDate;
	private String actualsprintEndDate;
	private Integer estimatedHours;
	private Integer actualHours;
	private Integer totalTasks;

	public List<UserReport> getIssues()
	{
		return issues;
	}

	public void setIssues( List<UserReport> issues )
	{
		this.issues = issues;
	}

	public String getSprintName()
	{
		return sprintName;
	}

	public void setSprintName( String sprintName )
	{
		this.sprintName = sprintName;
	}

	public String getSprintStartDate()
	{
		return sprintStartDate;
	}

	public void setSprintStartDate( String sprintStartDate )
	{
		this.sprintStartDate = sprintStartDate;
	}

	public String getSprintEndDate()
	{
		return sprintEndDate;
	}

	public void setSprintEndDate( String sprintEndDate )
	{
		this.sprintEndDate = sprintEndDate;
	}

	public String getActualsprintEndDate()
	{
		return actualsprintEndDate;
	}

	public void setActualsprintEndDate( String actualsprintEndDate )
	{
		this.actualsprintEndDate = actualsprintEndDate;
	}

	public Integer getEstimatedHours()
	{
		return estimatedHours;
	}

	public void setEstimatedHours( Integer estimatedHours )
	{
		this.estimatedHours = estimatedHours;
	}

	public Integer getActualHours()
	{
		return actualHours;
	}

	public void setActualHours( Integer actualHours )
	{
		this.actualHours = actualHours;
	}

	public Integer getTotalTasks()
	{
		return totalTasks;
	}

	public void setTotalTasks( Integer totalTasks )
	{
		this.totalTasks = totalTasks;
	}

}
