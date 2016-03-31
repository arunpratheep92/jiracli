package com.razorthink.jira.cli.worklogReport.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import com.razorthink.jira.cli.utils.ConvertToCSV;
import com.razorthink.jira.cli.worklogReport.service.WorklogReportService;
import com.razorthink.utils.cmutils.NullEmptyUtils;

@Service
public class WorklogReportServiceImpl implements WorklogReportService {

	@Autowired
	private Environment env;

	private static final Logger logger = LoggerFactory.getLogger(WorklogReportServiceImpl.class);

	/**
	 * Generates a report of the worklog for the day specified in the argument
	 * 
	 * @param params contains
	 * <ul>
	 * <li><strong>project</strong> Name of the project 
	 * <li><strong>date</strong> Date for which report is to be generated
	 * </ul>
	 * @param restClient It is used to make Rest calls to Jira to fetch sprint details
	 * @return Complete url of the worklog report generated
	 * 
	 * @throws DataException If some internal error occurs
	 */
	@Override
	public String getWorklogReport( Map<String, String> params, JiraRestClient restClient )
	{
		logger.debug("getWorklogReport");
		String date = params.get("date").trim();
		String project = params.get("project").trim();
		List<TimesheetReport> report = new ArrayList<>();
		Iterable<Issue> retrievedIssue = restClient.getSearchClient()
				.searchJql(" worklogDate = '" + date + "' AND project = '" + project + "' ORDER BY assignee", 1000, 0,
						null)
				.claim().getIssues();
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
						StringBuilder sprint = new StringBuilder("");
						Worklog worklog = iterator.next();
						if( worklog.getUpdateDate().toString("yyyy/MM/dd").equals(date)
								|| worklog.getUpdateDate().toString("yyyy-MM-dd").equals(date) )
						{
							TimesheetReport timesheetReport = new TimesheetReport();
							timesheetReport.setProject(issue.get().getProject().getName());
							timesheetReport.setKey(issue.get().getKey());
							timesheetReport.setType(issue.get().getIssueType().getName());
							timesheetReport.setTitle(issue.get().getSummary());
							timesheetReport.setUsername(worklog.getUpdateAuthor().getDisplayName());
							if( issue.get().getFieldByName("Sprint") != null
									&& issue.get().getFieldByName("Sprint").getValue() != null )
							{
								Pattern pattern = Pattern.compile("\\[\".*\\[.*,name=(.*),startDate=(.*),.*\\]");
								Matcher matcher = pattern
										.matcher(issue.get().getFieldByName("Sprint").getValue().toString());
								while( matcher.find() )
								{
									sprint.append(matcher.group(1)).append(" ");
								}
								timesheetReport.setSprint(sprint.toString());
							}
							else
							{
								timesheetReport.setSprint("null");
							}
							timesheetReport.setDate(worklog.getUpdateDate().toString("MM/dd/yy HH:mm:ss"));
							timesheetReport.setTimeSpent(worklog.getMinutesSpent() / 60D);
							timesheetReport.setComment(worklog.getComment());
							report.add(timesheetReport);
						}
					}
				}
			}
			catch( InterruptedException | ExecutionException e )
			{
				logger.error("Error:" + e.getMessage());
				throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
			}
		}
		String filename = project + "_" + date + "_worklog.csv";
		filename = filename.replace(" ", "_");
		filename = filename.replace("/", "_");
		ConvertToCSV exportToCSV = new ConvertToCSV();
		exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, report);
		return env.getProperty("csv.aliaspath") + filename;
	}
}
