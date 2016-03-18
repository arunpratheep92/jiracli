package com.razorthink.jira.cli.timesheet.service;

import java.util.List;
import java.util.Map;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.jira.cli.domain.TimesheetReport;

public interface TimesheetReportService {

	List<TimesheetReport> getTimesheetReport( Map<String, String> params, JiraRestClient restClient );

}