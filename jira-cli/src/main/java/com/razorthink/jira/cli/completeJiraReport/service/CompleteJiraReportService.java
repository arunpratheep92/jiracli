package com.razorthink.jira.cli.completeJiraReport.service;

import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;

public interface CompleteJiraReportService {

	String getCompleteJiraReport( Map<String, String> params, JiraRestClient restClient );

}