package com.razorthink.jira.cli.service;

import java.util.List;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.razorthink.jira.cli.domain.JiraIssue;

public interface JiraService {

	public Boolean validate( List<String> commandToken );

	public Boolean login( String username, String password, String url );

	public List<BasicProject> getAllProjects();

	public String getStatus( String issue, String project );

	public String getIssueType( String issue, String project );

	public String getComponents( String issue, String project );

	public String getDesription( String issue, String project );

	public String getReporter( String issue, String project );

	public String getAssignee( String issue, String project );

	public String getResolution( String issue, String project );

	public String getCreationDate( String issue, String project );

	public String getUpdateDate( String issue, String project );

	public String getDueDate( String issue, String project );

	public String getPriority( String issue, String project );

	public String getVotes( String issue, String project );

	public String getFixVersions( String issue, String project );

	public String getComments( String issue, String project );

	public String getWatchers( String issue, String project );

	public String getLabels( String issue, String project );

	public List<JiraIssue> getJqlResult( String jqlValue );

	public List<JiraIssue> getAllIssues( List<String> commandToken );

	public String getHelp();
}
