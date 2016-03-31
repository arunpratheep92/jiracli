package com.razorthink.jira.cli.sprintReport.service;

import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import net.rcarz.jiraclient.JiraClient;

public interface SprintReportService {

	String getSprintReport( Map<String, String> params, JiraRestClient restClient, JiraClient jiraClient );

}