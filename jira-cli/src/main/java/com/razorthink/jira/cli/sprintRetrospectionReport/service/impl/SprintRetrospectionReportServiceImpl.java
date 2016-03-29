package com.razorthink.jira.cli.sprintRetrospectionReport.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
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
import com.razorthink.jira.cli.domain.IncompletedIssues;
import com.razorthink.jira.cli.domain.SprintRetrospection;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.sprintRetrospectionReport.service.SprintRetrospectionReportService;
import com.razorthink.jira.cli.utils.ConvertToCSV;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;
import net.rcarz.jiraclient.greenhopper.SprintIssue;

@Service
public class SprintRetrospectionReportServiceImpl implements SprintRetrospectionReportService {

	@Autowired
	private Environment env;
	private static final Logger logger = LoggerFactory.getLogger(SprintRetrospectionReportServiceImpl.class);

	/* (non-Javadoc)
	 * @see com.razorthink.jira.cli.sprintRetrospectionReport.service.impl.SprintRetrospectionReportService#getSprintRetrospectionReport(java.util.Map, com.atlassian.jira.rest.client.api.JiraRestClient)
	 */
	@Override
	public String getSprintRetrospectionReport( Map<String, String> params, JiraRestClient restClient,
			JiraClient jiraClient, GreenHopperClient gh )
	{
		logger.debug("getSprintRetrospectionReport");
		String project = params.get("project");
		String sprint = params.get("sprint");
		int rvId = 0;
		int sprintId = 0;
		Double actualHours = 0.0;
		Double estimatedHours = 0.0;
		Integer totalTasks = 0;
		Integer incompletedTasks = 0;
		Double availableHours = 0.0;
		Double surplus = 0.0;
		DateTime startDt = null;
		DateTime endDt = null;
		DateTime tempDate = null;
		DateTime completeDate = null;
		List<SprintRetrospection> sprintRetrospectionReport = new ArrayList<>();
		List<String> incompleteIssueKeys = new ArrayList<>();
		Set<String> assignee = new TreeSet<>();
		if( project == null || sprint == null )
		{
			logger.error("Error: Missing required paramaters");
			throw new DataException(HttpStatus.BAD_REQUEST.toString(), "Missing required paramaters");
		}
		Iterable<Issue> retrievedIssue = restClient.getSearchClient().searchJql(" sprint = '" + sprint
				+ "' AND project = '" + project + "' AND assignee is not EMPTY ORDER BY assignee", 1000, 0, null)
				.claim().getIssues();
		Pattern pattern = Pattern.compile(
				"\\[\".*\\[id=(.*),rapidViewId=(.*),.*,name=(.*),startDate=(.*),endDate=(.*),completeDate=(.*),.*\\]");
		Matcher matcher = pattern
				.matcher(retrievedIssue.iterator().next().getFieldByName("Sprint").getValue().toString());
		while( matcher.find() )
		{
			if( matcher.group(3).equals(sprint) )
			{
				startDt = new DateTime(matcher.group(4));
				endDt = new DateTime(matcher.group(5));
				completeDate = new DateTime(matcher.group(6));
				sprintId = Integer.parseInt(matcher.group(1));
				rvId = Integer.parseInt(matcher.group(2));
			}

		}
		try
		{
			IncompletedIssues incompletedIssues = IncompletedIssues.get(jiraClient.getRestClient(), rvId, sprintId);
			for( SprintIssue issueValue : incompletedIssues.getIncompleteIssues() )
			{
				incompleteIssueKeys.add(issueValue.getKey());
			}
		}
		catch( JiraException e )
		{
			logger.error("Error:" + e.getMessage());
			throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
		}
		for( Issue issueValue : retrievedIssue )
		{
			if( !assignee.contains(issueValue.getAssignee().getDisplayName()) )
			{
				Iterable<Issue> assigneeIssue = restClient
						.getSearchClient().searchJql("assignee = '" + issueValue.getAssignee().getName()
								+ "' AND sprint = '" + sprint + "' AND project = '" + project + "'")
						.claim().getIssues();
				SprintRetrospection sprintRetrospection = new SprintRetrospection();
				actualHours = 0.0;
				estimatedHours = 0.0;
				totalTasks = 0;
				incompletedTasks = 0;
				for( Issue assigneeIssueValue : assigneeIssue )
				{
					Promise<Issue> issue = restClient.getIssueClient().getIssue(assigneeIssueValue.getKey());
					try
					{
						if( issue.get().getTimeTracking() != null )
						{
							if( issue.get().getTimeTracking().getOriginalEstimateMinutes() != null )
							{
								estimatedHours += issue.get().getTimeTracking().getOriginalEstimateMinutes();
							}
							if( issue.get().getTimeTracking().getTimeSpentMinutes() != null )
							{
								if( issue.get().getFieldByName("Sprint").getValue().toString().contains("\",\"com") )
								{
									Iterable<Worklog> worklogList = issue.get().getWorklogs();
									for( Worklog worklog : worklogList )
									{
										if( worklog.getUpdateDate().compareTo(startDt) >= 0 && ((completeDate != null
												&& (worklog.getUpdateDate().compareTo(completeDate) <= 0))
												|| completeDate == null
														&& ((worklog.getUpdateDate().compareTo(endDt) <= 0))) )
										{
											actualHours += worklog.getMinutesSpent();
										}
									}
								}
								else
								{
									actualHours += issue.get().getTimeTracking().getTimeSpentMinutes();
								}
							}
						}
						if( incompleteIssueKeys.contains(issue.get().getKey()) )
						{
							incompletedTasks++;
						}
						totalTasks++;
					}
					catch( InterruptedException | ExecutionException e )
					{

					}
				}
				if( assignee.isEmpty() )
				{
					tempDate = new DateTime(startDt.getMillis());
					while( tempDate.compareTo(endDt) <= 0 )
					{
						if( tempDate.getDayOfWeek() != DateTimeConstants.SATURDAY
								&& tempDate.getDayOfWeek() != DateTimeConstants.SUNDAY )
						{
							availableHours += 1;
						}
						tempDate = tempDate.plusDays(1);

					}
					availableHours *= 8D;
				}
				estimatedHours /= 60D;
				actualHours /= 60D;
				surplus = availableHours - estimatedHours;
				sprintRetrospection.setAssignee(issueValue.getAssignee().getDisplayName());
				sprintRetrospection.setEstimatedHours(estimatedHours);
				sprintRetrospection.setTimeTaken(actualHours);
				sprintRetrospection.setAvailableHours(availableHours);
				sprintRetrospection.setSurplus(surplus);
				sprintRetrospection.setBuffer((surplus / availableHours) * 100);
				if( actualHours != 0 )
				{
					sprintRetrospection.setEfficiency(100 + ((estimatedHours - actualHours) / actualHours * 100));
				}
				else
				{
					sprintRetrospection.setEfficiency(0D);
				}
				sprintRetrospection.setTotalTasks(totalTasks);
				sprintRetrospection.setIncompletedIssues(incompletedTasks);
				sprintRetrospectionReport.add(sprintRetrospection);
				assignee.add(issueValue.getAssignee().getDisplayName());
			}
		}
		String filename = project + "_" + sprint + "_retrospection_report.csv";
		filename = filename.replace(" ", "_");
		ConvertToCSV exportToCSV = new ConvertToCSV();
		exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, sprintRetrospectionReport);
		return env.getProperty("csv.aliaspath") + filename;
	}
}
