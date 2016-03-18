package com.razorthink.jira.cli.sprintRetrospectionReport.service;

import java.util.List;
import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.jira.cli.domain.SprintRetrospection;

public interface SprintRetrospectionReportService {

	List<SprintRetrospection> getSprintRetrospectionReport( Map<String, String> params, JiraRestClient restClient );

}