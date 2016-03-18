package com.razorthink.jira.cli.domain;

public class JiraReportIssue {

	private String epicKey;
	private String key;
	private String status;
	private String issueType;
	private String project;
	private String summary;
	private String reporter;
	private String reporterDiplayName;
	private String assignee;
	private String assigneeDiplayName;
	private String creationDate;
	private String updateDate;
	private String priority;
	private Integer originalEstimateMinutes;
	private Integer remainingEstimateMinutes;
	private Integer timeSpentMinutes;
	private String sprint;

	public String getEpicKey()
	{
		return epicKey;
	}

	public void setEpicKey( String epicKey )
	{
		this.epicKey = epicKey;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey( String key )
	{
		this.key = key;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus( String status )
	{
		this.status = status;
	}

	public String getIssueType()
	{
		return issueType;
	}

	public void setIssueType( String issueType )
	{
		this.issueType = issueType;
	}

	public String getProject()
	{
		return project;
	}

	public void setProject( String project )
	{
		this.project = project;
	}

	public String getSummary()
	{
		return summary;
	}

	public void setSummary( String summary )
	{
		this.summary = summary;
	}

	public String getReporter()
	{
		return reporter;
	}

	public void setReporter( String reporter )
	{
		this.reporter = reporter;
	}

	public String getReporterDiplayName()
	{
		return reporterDiplayName;
	}

	public void setReporterDiplayName( String reporterDiplayName )
	{
		this.reporterDiplayName = reporterDiplayName;
	}

	public String getAssignee()
	{
		return assignee;
	}

	public void setAssignee( String assignee )
	{
		this.assignee = assignee;
	}

	public String getAssigneeDiplayName()
	{
		return assigneeDiplayName;
	}

	public void setAssigneeDiplayName( String assigneeDiplayName )
	{
		this.assigneeDiplayName = assigneeDiplayName;
	}

	public String getCreationDate()
	{
		return creationDate;
	}

	public void setCreationDate( String creationDate )
	{
		this.creationDate = creationDate;
	}

	public String getUpdateDate()
	{
		return updateDate;
	}

	public void setUpdateDate( String updateDate )
	{
		this.updateDate = updateDate;
	}

	public String getPriority()
	{
		return priority;
	}

	public void setPriority( String priority )
	{
		this.priority = priority;
	}

	public Integer getOriginalEstimateMinutes()
	{
		return originalEstimateMinutes;
	}

	public void setOriginalEstimateMinutes( Integer originalEstimateMinutes )
	{
		this.originalEstimateMinutes = originalEstimateMinutes;
	}

	public Integer getRemainingEstimateMinutes()
	{
		return remainingEstimateMinutes;
	}

	public void setRemainingEstimateMinutes( Integer remainingEstimateMinutes )
	{
		this.remainingEstimateMinutes = remainingEstimateMinutes;
	}

	public Integer getTimeSpentMinutes()
	{
		return timeSpentMinutes;
	}

	public void setTimeSpentMinutes( Integer timeSpentMinutes )
	{
		this.timeSpentMinutes = timeSpentMinutes;
	}

	public String getSprint()
	{
		return sprint;
	}

	public void setSprint( String sprint )
	{
		this.sprint = sprint;
	}
}
