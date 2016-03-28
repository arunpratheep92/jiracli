package com.razorthink.jira.cli.sprintReport.service;

import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;

public interface SprintReportService {

	String getSprintReport( Map<String, String> params, JiraRestClient restClient, JiraClient jiraClient,
			GreenHopperClient gh );

}