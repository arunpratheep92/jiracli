package com.razorthink.jira.cli.completeJiraReport.service;

import java.util.List;
import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.jira.cli.domain.JiraReportIssue;

public interface CompleteJiraReportService {

	List<JiraReportIssue> getCompleteJiraReport( Map<String, String> params, JiraRestClient restClient );

}