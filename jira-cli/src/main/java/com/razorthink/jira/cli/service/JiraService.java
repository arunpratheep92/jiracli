package com.razorthink.jira.cli.service;

import java.util.List;

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.razorthink.jira.cli.domain.JiraIssue;

public interface JiraService {
	
	public Boolean validate(String[] commandTokens);
	public Boolean login(String username,String password,String url);
	public List<JiraIssue> jqlBuilder(String[] commandTokens);
	public List<BasicProject> getAllProjects();
	public String getStatus(String issue, String project);
	public String getIssueType(String issue, String project);
	public String getComponents(String issue, String project);
}
