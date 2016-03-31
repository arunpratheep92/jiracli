package com.razorthink.jira.cli.worklogReport.service;

import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;

public interface WorklogReportService {

	String getWorklogReport( Map<String, String> params, JiraRestClient restClient );

}