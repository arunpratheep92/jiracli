package com.razorthink.jira.cli.domain;

public class SprintRetrospection {

	private String assignee;
	private Double estimatedHours;
	private Double availableHours;
	private Double surplus;
	private Double buffer;
	private Double efficiency;
	private Double timeTaken;
	private Integer totalTasks;
	private Integer incompletedIssues;

	public String getAssignee()
	{
		return assignee;
	}

	public void setAssignee( String assignee )
	{
		this.assignee = assignee;
	}

	public Double getEstimatedHours()
	{
		return estimatedHours;
	}

	public void setEstimatedHours( Double estimatedHours )
	{
		this.estimatedHours = estimatedHours;
	}

	public Double getAvailableHours()
	{
		return availableHours;
	}

	public void setAvailableHours( Double availableHours )
	{
		this.availableHours = availableHours;
	}

	public Double getSurplus()
	{
		return surplus;
	}

	public void setSurplus( Double surplus )
	{
		this.surplus = surplus;
	}

	public Double getBuffer()
	{
		return buffer;
	}

	public void setBuffer( Double buffer )
	{
		this.buffer = buffer;
	}

	public Double getEfficiency()
	{
		return efficiency;
	}

	public void setEfficiency( Double efficiency )
	{
		this.efficiency = efficiency;
	}

	public Double getTimeTaken()
	{
		return timeTaken;
	}

	public void setTimeTaken( Double timeTaken )
	{
		this.timeTaken = timeTaken;
	}

	public Integer getTotalTasks()
	{
		return totalTasks;
	}

	public void setTotalTasks( Integer totalTasks )
	{
		this.totalTasks = totalTasks;
	}

	public Integer getIncompletedIssues()
	{
		return incompletedIssues;
	}

	public void setIncompletedIssues( Integer incompletedIssues )
	{
		this.incompletedIssues = incompletedIssues;
	}
}
