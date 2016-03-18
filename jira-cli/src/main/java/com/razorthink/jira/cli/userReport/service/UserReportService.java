package com.razorthink.jira.cli.userReport.service;

import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.jira.cli.domain.AggregateUserReport;

public interface UserReportService {

	AggregateUserReport getUserReport( Map<String, String> params, JiraRestClient restClient );

}