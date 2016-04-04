package com.razorthink.jira.cli.backlog.service.impl;

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
import com.razorthink.jira.cli.backlog.service.BacklogReportService;
import com.razorthink.jira.cli.domain.UserReport;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.utils.ConvertToCSV;
import com.razorthink.utils.cmutils.NullEmptyUtils;

/**
 * 
 * @author arun
 *
 */
@Service
public class BacklogReportServiceImpl implements BacklogReportService {

	@Autowired
	private Environment env;
	private static final Logger logger = LoggerFactory.getLogger(BacklogReportServiceImpl.class);

	/**
	 * Generates a backlog report of the project mentioned 
	 * 
	 * @param params Contains the name of the project whose backlog report is to be generated
	 * @param restClient It is used to make Rest calls to Jira to fetch backlog details
	 * @return Complete url of the backlog report generated
	 */
	@Override
	public String getBacklogReport( Map<String, String> params, JiraRestClient restClient )
	{
		logger.debug("getBacklogReport");
		String project = params.get("project");
		if( project == null )
		{
			logger.error("Error: Missing required paramaters");
			throw new DataException(HttpStatus.BAD_REQUEST.toString(), "Missing required paramaters");
		}
		List<UserReport> issueList = new ArrayList<>();
		Iterable<Issue> retrievedIssue = restClient.getSearchClient()
				.searchJql(
						"project = '" + project
								+ "' AND sprint is EMPTY AND resolution = Unresolved and status != Closed",
						1000, 0, null)
				.claim().getIssues();
		for( Issue issueValue : retrievedIssue )
		{
			//Retrieve issues for a particular key that contains TimeTracking details
			Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
			UserReport userReport = new UserReport();
			try
			{
				StringBuilder sprint = new StringBuilder("");
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
						Pattern pattern = Pattern.compile("\\[\".*\\[.*,name=(.*),startDate=(.*),.*\\]");
						Matcher matcher = pattern.matcher(issue.get().getFieldByName("Sprint").getValue().toString());
						while( matcher.find() )
						{
							sprint.append(matcher.group(1)).append(" ");
						}
						userReport.setSprint(sprint.toString());
					}
					else
					{
						userReport.setSprint("null");
					}
				}
				issueList.add(userReport);
			}
			catch( InterruptedException | ExecutionException e )
			{
				logger.error("Error:" + e.getMessage());
				throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
			}
		}
		String filename = project + "_backlog.csv";
		filename = filename.replace(" ", "_");
		ConvertToCSV exportToCSV = new ConvertToCSV();
		exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, issueList);
		return env.getProperty("csv.aliaspath") + filename + " ";
	}
}
