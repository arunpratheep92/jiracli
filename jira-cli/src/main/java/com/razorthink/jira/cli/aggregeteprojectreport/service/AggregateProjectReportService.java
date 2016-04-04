package com.razorthink.jira.cli.aggregeteprojectreport.service;

import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;

public interface AggregateProjectReportService {

	String getAggregateProjectReport( Map<String, String> params, JiraRestClient restClient, JiraClient jiraClient, GreenHopperClient gh );

}