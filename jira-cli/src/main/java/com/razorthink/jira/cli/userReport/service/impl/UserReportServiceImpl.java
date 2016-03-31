package com.razorthink.jira.cli.userReport.service.impl;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
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
import com.atlassian.util.concurrent.Promise;
import com.razorthink.jira.cli.domain.AggregateUserReport;
import com.razorthink.jira.cli.domain.UserReport;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.userReport.service.UserReportService;
import com.razorthink.jira.cli.utils.ConvertToCSV;
import com.razorthink.utils.cmutils.NullEmptyUtils;

/**
 * 
 * @author arun
 *
 */
@Service
public class UserReportServiceImpl implements UserReportService {

	@Autowired
	private Environment env;

	private static final Logger logger = LoggerFactory.getLogger(UserReportServiceImpl.class);

	/**
	 * Generates a report of the issues that a particular user worked on a particular sprint
	 * 
	 * @param params contains
	 * <ul>
	 * <li><strong>project</strong> Name of the project 
	 * <li><strong>sprint</strong> Name of the sprint
	 * <li><strong>user</strong> Name of the user for which report is to be generated
	 * <li><strong>export</strong> If true exports the report to csv
	 * </ul>
	 * @param restClient It is used to make Rest calls to Jira to fetch sprint details
	 * @return Complete url of the user report generated
	 * 
	 * @throws DataException If some internal error occurs
	 */
	@Override
	public AggregateUserReport getUserReport( Map<String, String> params, JiraRestClient restClient )
	{
		logger.debug("getUserReport");
		String sprint = params.get("sprint");
		String user = params.get("user");
		String project = params.get("project");
		String export = params.get("export");
		Integer actualHours = 0;
		Integer estimatedHours = 0;
		Integer totalTasks = 0;
		String sprintValue = null;
		AggregateUserReport report = new AggregateUserReport();
		List<UserReport> issueList = new ArrayList<>();
		Iterable<Issue> retrievedIssue = restClient.getSearchClient()
				.searchJql("assignee = '" + user + "' AND sprint = '" + sprint + "' AND project = '" + project + "'",
						1000, 0, null)
				.claim().getIssues();
		for( Issue issueValue : retrievedIssue )
		{
			Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
			UserReport userReport = new UserReport();
			try
			{
				userReport.setKey(issue.get().getKey());
				userReport.setStatus(issue.get().getStatus().getName());
				userReport.setIssueType(issue.get().getIssueType().getName());
				userReport.setProject(issue.get().getProject().getName());
				userReport.setSummary(issue.get().getSummary());
				userReport.setReporter(issue.get().getReporter().getName());
				userReport.setReporterDiplayName(issue.get().getReporter().getDisplayName());
				if( issue.get().getAssignee() != null )
				{
					userReport.setAssignee(issue.get().getAssignee().getName());
					userReport.setAssigneeDiplayName(issue.get().getAssignee().getDisplayName());
				}
				else
				{
					userReport.setAssignee("Unassigned");
				}
				userReport.setCreationDate(issue.get().getCreationDate().toString("MM/dd/yy HH:mm:ss"));
				if( issue.get().getUpdateDate() != null )
				{
					userReport.setUpdateDate(issue.get().getUpdateDate().toString("MM/dd/yy HH:mm:ss"));
				}
				else
				{
					userReport.setUpdateDate("null");
				}
				if( issue.get().getPriority() != null )
				{
					userReport.setPriority(issue.get().getPriority().getName());
				}
				else
				{
					userReport.setPriority("null");
				}
				if( issue.get().getTimeTracking() != null )
				{
					if( issue.get().getTimeTracking().getOriginalEstimateMinutes() != null )
					{
						userReport
								.setOriginalEstimateMinutes(issue.get().getTimeTracking().getOriginalEstimateMinutes());
					}
					else
					{
						userReport.setOriginalEstimateMinutes(0);
					}
					if( issue.get().getTimeTracking().getTimeSpentMinutes() != null )
					{
						userReport.setTimeSpentMinutes(issue.get().getTimeTracking().getTimeSpentMinutes());
					}
					else
					{
						userReport.setTimeSpentMinutes(0);
					}
					if( issue.get().getTimeTracking().getRemainingEstimateMinutes() != null )
					{
						userReport.setRemainingEstimateMinutes(
								issue.get().getTimeTracking().getRemainingEstimateMinutes());
					}
					else
					{
						userReport.setRemainingEstimateMinutes(0);
					}
					if( issue.get().getTimeTracking().getOriginalEstimateMinutes() != null )
					{
						estimatedHours += issue.get().getTimeTracking().getOriginalEstimateMinutes();
					}
					if( issue.get().getTimeTracking().getTimeSpentMinutes() != null )
					{
						actualHours += issue.get().getTimeTracking().getTimeSpentMinutes();
					}
				}
				if( !NullEmptyUtils.isNullorEmpty((List<?>) issue.get().getFields()) )
				{
					if( issue.get().getFieldByName("Epic Link") != null
							&& issue.get().getFieldByName("Epic Link").getValue() != null )
					{
						userReport.setEpicLink(issue.get().getFieldByName("Epic Link").getValue().toString());
					}
					else
					{
						userReport.setEpicLink("null");
					}
					if( issue.get().getFieldByName("Sprint") != null
							&& issue.get().getFieldByName("Sprint").getValue() != null )
					{
						sprintValue = issue.get().getFieldByName("Sprint").getValue().toString();
						Pattern pattern = Pattern.compile("\\[\".*\\[.*,name=(.*),startDate=(.*),.*\\]");
						Matcher matcher = pattern.matcher(issue.get().getFieldByName("Sprint").getValue().toString());
						if( matcher.find() )
						{
							userReport.setSprint(matcher.group(1));
						}
					}
					else
					{
						userReport.setSprint("null");
					}
				}
				issueList.add(userReport);
				totalTasks++;
			}
			catch( InterruptedException | ExecutionException e )
			{
				logger.error("Error:" + e.getMessage());
				throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
			}
		}
		if( export.equals("true") )
		{
			String filename = project + "_" + sprint + "_" + user + ".csv";
			filename = filename.replace(" ", "_");
			ConvertToCSV exportToCSV = new ConvertToCSV();
			exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, issueList);
			report.setFilepath(env.getProperty("csv.aliaspath") + filename);
		}
		report.setIssues(issueList);
		Pattern patter = Pattern
				.compile("\\[\".*\\[.*,state=(.*),name=(.*),startDate=(.*),endDate=(.*),completeDate=(.*),.*\\]");
		Matcher matcher = patter.matcher(sprintValue);
		if( matcher.find() )
		{
			report.setSprintName(matcher.group(2));
			TemporalAccessor temporal = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
					.parse(matcher.group(3));
			String startdate = DateTimeFormatter.ofPattern("dd/MMM/yy,HH:mm").format(temporal);
			report.setSprintStartDate(startdate);
			temporal = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(matcher.group(4));
			String endDate = DateTimeFormatter.ofPattern("dd/MMM/yy,HH:mm").format(temporal);
			report.setSprintEndDate(endDate);
			if( matcher.group(5).equals("<null>") )
			{
				report.setActualsprintEndDate("Open Sprint");
			}
			else
			{
				temporal = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(matcher.group(5));
				String actualEndDate = DateTimeFormatter.ofPattern("dd/MMM/yy,HH:mm").format(temporal);
				report.setActualsprintEndDate(actualEndDate);
			}
			actualHours = actualHours / 60;
			report.setActualHours(actualHours);
			estimatedHours = estimatedHours / 60;
			report.setEstimatedHours(estimatedHours);
			report.setTotalTasks(totalTasks);
		}
		return report;
	}
}
