package com.razorthink.jira.cli.domain;

public class SprintReport {

	private String issueKey;
	private String issueType;
	private String issueSummary;
	private String assignee;
	private String estimatedHours;
	private String loggedHours;
	private String status;

	public String getIssueKey()
	{
		return issueKey;
	}

	public void setIssueKey( String issueKey )
	{
		this.issueKey = issueKey;
	}

	public String getIssueType()
	{
		return issueType;
	}

	public void setIssueType( String issueType )
	{
		this.issueType = issueType;
	}

	public String getIssueSummary()
	{
		return issueSummary;
	}

	public void setIssueSummary( String issueSummary )
	{
		this.issueSummary = issueSummary;
	}

	public String getAssignee()
	{
		return assignee;
	}

	public void setAssignee( String assignee )
	{
		this.assignee = assignee;
	}

	public String getEstimatedHours()
	{
		return estimatedHours;
	}

	public void setEstimatedHours( String estimatedHours )
	{
		this.estimatedHours = estimatedHours;
	}

	public String getLoggedHours()
	{
		return loggedHours;
	}

	public void setLoggedHours( String loggedHours )
	{
		this.loggedHours = loggedHours;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus( String status )
	{
		this.status = status;
	}

}
