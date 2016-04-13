package com.razorthink.jira.cli.sprintRetrospectionReport.service.impl;

import java.text.DecimalFormat;
import java.time.ZoneId;
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
import org.joda.time.DateTimeZone;
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
import net.rcarz.jiraclient.greenhopper.SprintIssue;

/**
 * 
 * @author arun
 *
 */
@Service
public class SprintRetrospectionReportServiceImpl implements SprintRetrospectionReportService {

	@Autowired
	private Environment env;
	private static final Logger logger = LoggerFactory.getLogger(SprintRetrospectionReportServiceImpl.class);

	/**
	 * Generates a Sprint Retrospection report of the sprint specified in the argument
	 * 
	 * @param params contains
	 * <ul>
	 * <li><strong>project</strong> Name of the project 
	 * <li><strong>sprint</strong> Name of the sprint for which report is to be generated
	 * <li><strong>assignee name</strong> OPTIONAL Available hours of the assignee
	 * <li><strong>avilableHours</strong> OPTIONAL Default Available hours for all assignees
	 * </ul>
	 * 
	 * @param restClient It is used to make Rest calls to Jira to fetch sprint details
	 * @param jiraClient It is used to fetch removed issues and issues added during a sprint
	 * @return Complete url of the sprint Retrospection report generated
	 * 
	 * @throws DataException If some internal error occurs
	 */
	@Override
	public String getSprintRetrospectionReport( Map<String, String> params, JiraRestClient restClient,
			JiraClient jiraClient )
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
		String jql = null;
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
				startDt = new DateTime(matcher.group(4), DateTimeZone.forID(ZoneId.of("+05:30").toString()));
				endDt = new DateTime(matcher.group(5), DateTimeZone.forID(ZoneId.of("+05:30").toString()));
				if( !matcher.group(6).equals("<null>") )
				{
					completeDate = new DateTime(matcher.group(6), DateTimeZone.forID(ZoneId.of("+05:30").toString()));
				}
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
			availableHours = 0.0;
			if( !assignee.contains(issueValue.getAssignee().getDisplayName()) )
			{
				if( completeDate != null )
				{
					jql = " issue in workedIssues(\"" + startDt.toString("yyyy/MM/dd") + "\",\""
							+ completeDate.toString("yyyy/MM/dd") + "\", " + issueValue.getAssignee().getName()
							+ ") AND assignee is not EMPTY ORDER BY assignee ASC";
				}
				else
				{
					jql = " issue in workedIssues(\"" + startDt.toString("yyyy/MM/dd") + "\",\""
							+ endDt.toString("yyyy/MM/dd") + "\"," + issueValue.getAssignee().getName()
							+ ") AND assignee is not EMPTY ORDER BY assignee ASC";
				}
				Iterable<Issue> assigneeIssue = restClient.getSearchClient().searchJql(jql, 1000, 0, null).claim()
						.getIssues();
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
								if( issueValue.getAssignee().getName()
										.equals(assigneeIssueValue.getAssignee().getName()) )
								{
									estimatedHours += issue.get().getTimeTracking().getOriginalEstimateMinutes();
								}
							}
							if( issue.get().getTimeTracking().getTimeSpentMinutes() != null )
							{
								Iterable<Worklog> worklogList = issue.get().getWorklogs();
								for( Worklog worklog : worklogList )
								{
									if( (worklog.getUpdateDate().compareTo(startDt) >= 0 && ((completeDate != null
											&& (worklog.getUpdateDate().compareTo(completeDate) <= 0))
											|| completeDate == null
													&& ((worklog.getUpdateDate().compareTo(endDt) <= 0))))
											&& worklog.getUpdateAuthor().getName()
													.equals(issueValue.getAssignee().getName()) )
									{
										actualHours += worklog.getMinutesSpent();
									}
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
						logger.error("Error:" + e.getMessage());
						throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
					}
				}
				tempDate = new DateTime(startDt.getMillis(), DateTimeZone.forID(ZoneId.of("+05:30").toString()));
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
				estimatedHours /= 60D;
				actualHours /= 60D;
				if( params.get("availableHours") != null )
				{
					availableHours = Double.parseDouble(params.get("availableHours"));
				}
				if( params.get(issueValue.getAssignee().getName()) != null )
				{
					availableHours = Double.parseDouble(params.get(issueValue.getAssignee().getName()));
				}
				surplus = availableHours - estimatedHours;
				sprintRetrospection.setAssignee(issueValue.getAssignee().getDisplayName());
				sprintRetrospection
						.setEstimatedHours(Double.parseDouble(new DecimalFormat("##.##").format(estimatedHours)));
				sprintRetrospection.setTimeTaken(Double.parseDouble(new DecimalFormat("##.##").format(actualHours)));
				sprintRetrospection.setAvailableHours(availableHours);
				sprintRetrospection.setSurplus(surplus);
				sprintRetrospection.setBuffer(
						Double.parseDouble(new DecimalFormat("##.##").format((surplus / availableHours) * 100)));
				if( actualHours != 0 )
				{
					sprintRetrospection.setEfficiency(Double.parseDouble(new DecimalFormat("##.##")
							.format(100 + ((estimatedHours - actualHours) / actualHours * 100))));
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
