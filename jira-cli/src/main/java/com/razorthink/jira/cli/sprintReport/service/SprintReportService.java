package com.razorthink.jira.cli.sprintReport.service;

import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;

public interface SprintReportService {

	String getSprintReport( Map<String, String> params, JiraRestClient restClient );

}