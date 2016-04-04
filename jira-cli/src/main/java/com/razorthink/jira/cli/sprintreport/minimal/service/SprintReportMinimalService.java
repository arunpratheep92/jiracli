package com.razorthink.jira.cli.sprintreport.minimal.service;

import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import net.rcarz.jiraclient.JiraClient;

public interface SprintReportMinimalService {

	String getMininmalSprintReport( Map<String, String> params, JiraRestClient restClient, JiraClient jiraClient );

}