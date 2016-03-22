package com.razorthink.jira.cli.timesheet.service;

import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;

public interface TimesheetReportService {

	String getTimesheetReport( Map<String, String> params, JiraRestClient restClient );

}