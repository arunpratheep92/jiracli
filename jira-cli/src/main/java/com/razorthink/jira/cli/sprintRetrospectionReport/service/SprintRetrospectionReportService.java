package com.razorthink.jira.cli.sprintRetrospectionReport.service;

import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;

public interface SprintRetrospectionReportService {

	String getSprintRetrospectionReport( Map<String, String> params, JiraRestClient restClient );

}