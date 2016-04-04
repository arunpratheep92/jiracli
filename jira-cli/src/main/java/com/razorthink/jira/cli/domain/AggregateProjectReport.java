package com.razorthink.jira.cli.domain;

import java.util.List;

public class AggregateProjectReport {

	private Boolean isSprintfollowed;
	private Integer backlogCount;
	private List<SprintDetails> sprintDetails;

	public Boolean getIs_Sprint_followed()
	{
		return isSprintfollowed;
	}

	public void setIs_Sprint_followed( Boolean isSprintfollowed )
	{
		this.isSprintfollowed = isSprintfollowed;
	}

	public Integer getBacklogCount()
	{
		return backlogCount;
	}

	public void setBacklogCount( Integer backlogCount )
	{
		this.backlogCount = backlogCount;
	}

	public List<SprintDetails> getSprintDetails()
	{
		return sprintDetails;
	}

	public void setSprintDetails( List<SprintDetails> sprintDetails )
	{
		this.sprintDetails = sprintDetails;
	}

}
