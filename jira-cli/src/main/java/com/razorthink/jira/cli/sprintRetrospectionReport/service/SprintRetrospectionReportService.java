package com.razorthink.jira.cli.sprintRetrospectionReport.service;

import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;

public interface SprintRetrospectionReportService {

	String getSprintRetrospectionReport( Map<String, String> params, JiraRestClient restClient, JiraClient jiraClient,
			GreenHopperClient gh );

}