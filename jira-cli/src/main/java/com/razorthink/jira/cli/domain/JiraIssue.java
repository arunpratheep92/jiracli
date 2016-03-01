package com.razorthink.jira.cli.domain;

import java.util.List;
import java.util.Set;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;

public class JiraIssue {

	private String key;
	private String status;
	private String issueType;
	private String project;
	private List<String> components;
	private String summary;
	private String description;
	private String reporter;
	private String assignee;
	private String resolution;
	private String creationDate;
	private String updateDate;
	private String dueDate;
	private String priority;
	private List<String> fixVersions;
	private List<String> affectedVersions;
	private List<String> comments;
	private List<String> issueLinks;
	private TimeTracking timeTracking;
	private List<JiraSubtask> subtasks;
	private Set<String> labels;
	private String epicLink;
	private String sprint;

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

	public List<String> getComponents()
	{
		return components;
	}

	public void setComponents( List<String> components )
	{
		this.components = components;
	}

	public String getSummary()
	{
		return summary;
	}

	public void setSummary( String summary )
	{
		this.summary = summary;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription( String description )
	{
		this.description = description;
	}

	public String getReporter()
	{
		return reporter;
	}

	public void setReporter( String reporter )
	{
		this.reporter = reporter;
	}

	public String getAssignee()
	{
		return assignee;
	}

	public void setAssignee( String assignee )
	{
		this.assignee = assignee;
	}

	public String getResolution()
	{
		return resolution;
	}

	public void setResolution( String resolution )
	{
		this.resolution = resolution;
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

	public String getDueDate()
	{
		return dueDate;
	}

	public void setDueDate( String dueDate )
	{
		this.dueDate = dueDate;
	}

	public String getPriority()
	{
		return priority;
	}

	public void setPriority( String priority )
	{
		this.priority = priority;
	}

	public List<String> getFixVersions()
	{
		return fixVersions;
	}

	public void setFixVersions( List<String> fixVersions )
	{
		this.fixVersions = fixVersions;
	}

	public List<String> getAffectedVersions()
	{
		return affectedVersions;
	}

	public void setAffectedVersions( List<String> affectedVersions )
	{
		this.affectedVersions = affectedVersions;
	}

	public List<String> getComments()
	{
		return comments;
	}

	public void setComments( List<String> comments )
	{
		this.comments = comments;
	}

	public List<String> getIssueLinks()
	{
		return issueLinks;
	}

	public void setIssueLinks( List<String> issueLinks )
	{
		this.issueLinks = issueLinks;
	}

	public TimeTracking getTimeTracking()
	{
		return timeTracking;
	}

	public void setTimeTracking( TimeTracking timeTracking )
	{
		this.timeTracking = timeTracking;
	}

	public List<JiraSubtask> getSubtasks()
	{
		return subtasks;
	}

	public void setSubtasks( List<JiraSubtask> subtasks )
	{
		this.subtasks = subtasks;
	}

	public Set<String> getLabels()
	{
		return labels;
	}

	public void setLabels( Set<String> labels )
	{
		this.labels = labels;
	}

	public String getEpicLink()
	{
		return epicLink;
	}

	public void setEpicLink( String epicLink )
	{
		this.epicLink = epicLink;
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
