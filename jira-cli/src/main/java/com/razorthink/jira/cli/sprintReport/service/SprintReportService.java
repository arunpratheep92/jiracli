package com.razorthink.jira.cli.sprintReport.service;

import java.util.HashMap;
import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.jira.cli.domain.AggregateUserReport;

public interface SprintReportService {

	HashMap<String, AggregateUserReport> getSprintReport( Map<String, String> params, JiraRestClient restClient );

}