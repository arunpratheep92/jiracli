package com.razorthink.jira.cli.backlog.service;

import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;

public interface BacklogReportService {

	String getBacklogReport( Map<String, String> params, JiraRestClient restClient );

}