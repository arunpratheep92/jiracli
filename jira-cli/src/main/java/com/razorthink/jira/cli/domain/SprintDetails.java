package com.razorthink.jira.cli.domain;

public class SprintDetails {

	private String name;
	private String startDate;
	private String endDate;
	private String deliveryStatus;
	private String estimatedVsActualAccuracy;
	private String sprintChanges;
	private String estimateProvidedStatus;
	private String taskDescription_Statistics;

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getStartDate()
	{
		return startDate;
	}

	public void setStartDate( String startDate )
	{
		this.startDate = startDate;
	}

	public String getEndDate()
	{
		return endDate;
	}

	public void setEndDate( String endDate )
	{
		this.endDate = endDate;
	}

	public String getDeliveryStatus()
	{
		return deliveryStatus;
	}

	public void setDeliveryStatus( String deliveryStatus )
	{
		this.deliveryStatus = deliveryStatus;
	}

	public String getEstimatedVsActualAccuracy()
	{
		return estimatedVsActualAccuracy;
	}

	public void setEstimatedVsActualAccuracy( String estimatedVsActualAccuracy )
	{
		this.estimatedVsActualAccuracy = estimatedVsActualAccuracy;
	}

	public String getSprintChanges()
	{
		return sprintChanges;
	}

	public void setSprintChanges( String sprintChanges )
	{
		this.sprintChanges = sprintChanges;
	}

	public String getEstimateProvidedStatus()
	{
		return estimateProvidedStatus;
	}

	public void setEstimateProvidedStatus( String estimateProvidedStatus )
	{
		this.estimateProvidedStatus = estimateProvidedStatus;
	}

	public String getTaskDescription_Statistics()
	{
		return taskDescription_Statistics;
	}

	public void setTaskDescription_Statistics( String taskDescription_Statistics )
	{
		this.taskDescription_Statistics = taskDescription_Statistics;
	}

}
