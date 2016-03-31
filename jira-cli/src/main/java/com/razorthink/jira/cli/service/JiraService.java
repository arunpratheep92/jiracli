package com.razorthink.jira.cli.service;

import java.util.List;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.razorthink.jira.cli.domain.JiraIssue;

public interface JiraService {

	Boolean validate( List<String> commandToken );

	Boolean login( String username, String password, String url );

	List<BasicProject> getAllProjects();

	String getStatus( String issue, String project );

	String getIssueType( String issue, String project );

	String getComponents( String issue, String project );

	String getDesription( String issue, String project );

	String getReporter( String issue, String project );

	String getAssignee( String issue, String project );

	String getResolution( String issue, String project );

	String getCreationDate( String issue, String project );

	String getUpdateDate( String issue, String project );

	String getDueDate( String issue, String project );

	String getPriority( String issue, String project );

	String getVotes( String issue, String project );

	String getFixVersions( String issue, String project );

	String getComments( String issue, String project );

	String getWatchers( String issue, String project );

	String getLabels( String issue, String project );

	List<JiraIssue> getJqlResult( String jqlValue );

	List<JiraIssue> getAllIssues( List<String> commandToken );

	String getHelp();
}
