package com.razorthink.jira.cli.timesheet.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.atlassian.util.concurrent.Promise;
import com.razorthink.jira.cli.domain.TimesheetReport;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.timesheet.service.TimesheetReportService;
import com.razorthink.jira.cli.utils.ConvertToCSV;
import com.razorthink.utils.cmutils.NullEmptyUtils;

@Service
public class TimesheetReportServiceImpl implements TimesheetReportService {

	@Autowired
	private Environment env;

	private static final Logger logger = LoggerFactory.getLogger(TimesheetReportServiceImpl.class);

	/**
	 * Generates a timesheet report of the sprint specified in the argument
	 * 
	 * @param params contains
	 * <ul>
	 * <li><strong>project</strong> Name of the project 
	 * <li><strong>sprint</strong> Name of the sprint for which report is to be generated
	 * </ul>
	 * @param restClient It is used to make Rest calls to Jira to fetch sprint details
	 * @return Complete url of the timesheet report generated
	 * 
	 * @throws DataException If some internal error occurs
	 */
	@Override
	public String getTimesheetReport( Map<String, String> params, JiraRestClient restClient )
	{
		logger.debug("getTimesheetReport");
		String sprint = params.get("sprint");
		String project = params.get("project");
		Integer maxResults = 1000;
		Integer startAt = 0;
		List<TimesheetReport> report = new ArrayList<>();
		Iterable<Issue> retrievedIssue = restClient.getSearchClient()
				.searchJql(" sprint = '" + sprint + "' AND project = '" + project + "'", maxResults, startAt, null)
				.claim().getIssues();
		while( retrievedIssue.iterator().hasNext() )
		{
			for( Issue issueValue : retrievedIssue )
			{
				Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
				try
				{
					if( !NullEmptyUtils.isNullorEmpty((List<?>) issue.get().getWorklogs()) )
					{
						Iterator<Worklog> iterator = issue.get().getWorklogs().iterator();
						while( iterator.hasNext() )
						{
							Worklog worklog = iterator.next();
							TimesheetReport timesheetReport = new TimesheetReport();
							timesheetReport.setProject(issue.get().getProject().getName());
							timesheetReport.setKey(issue.get().getKey());
							timesheetReport.setType(issue.get().getIssueType().getName());
							timesheetReport.setTitle(issue.get().getSummary());
							timesheetReport.setUsername(worklog.getUpdateAuthor().getDisplayName());
							timesheetReport.setSprint(sprint);
							timesheetReport.setDate(worklog.getUpdateDate().toString("MM/dd/yy HH:mm:ss"));
							timesheetReport.setTimeSpent(worklog.getMinutesSpent() * 1D);
							timesheetReport.setComment(worklog.getComment());
							report.add(timesheetReport);
						}
					}
				}
				catch( InterruptedException | ExecutionException e )
				{
					logger.error("Error:" + e.getMessage());
					throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
				}
			}
			startAt += 1000;
			maxResults += 1000;
			retrievedIssue = restClient.getSearchClient()
					.searchJql(" sprint = '" + sprint + "' AND project = '" + project + "'", maxResults, startAt, null)
					.claim().getIssues();
		}
		String filename = project + "_" + sprint + "_timesheet.csv";
		filename = filename.replace(" ", "_");
		ConvertToCSV exportToCSV = new ConvertToCSV();
		exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, report);
		return env.getProperty("csv.aliaspath") + filename;
	}
}
