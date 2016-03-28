package com.razorthink.jira.cli.sprintReport.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.razorthink.jira.cli.domain.RemovedIssues;
import com.razorthink.jira.cli.domain.UserReport;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.sprintReport.service.SprintReportService;
import com.razorthink.jira.cli.userReport.service.UserReportService;
import com.razorthink.jira.cli.utils.ConvertToCSV;
import com.razorthink.utils.cmutils.NullEmptyUtils;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;
import net.rcarz.jiraclient.greenhopper.SprintIssue;

@Service
public class SprintReportServiceImpl implements SprintReportService {

	@Autowired
	private Environment env;

	@Autowired
	UserReportService userReportService;

	private static final Logger logger = LoggerFactory.getLogger(SprintReportServiceImpl.class);

	/* (non-Javadoc)
	 * @see com.razorthink.jira.cli.sprintReport.service.impl.SprintReport#getSprintReport(java.util.Map, com.atlassian.jira.rest.client.api.JiraRestClient)
	 */
	@Override
	public String getSprintReport( Map<String, String> params, JiraRestClient restClient, JiraClient jiraClient,
			GreenHopperClient gh )
	{
		logger.debug("getSprintReport");
		String sprint = params.get("sprint");
		String project = params.get("project");
		if( project == null || sprint == null )
		{
			logger.error("Error: Missing required paramaters");
			throw new DataException(HttpStatus.BAD_REQUEST.toString(), "Missing required paramaters");
		}
		int rvId = 0;
		int sprintId = 0;
		String assignee = null;
		AggregateUserReport aggregateUserReport = new AggregateUserReport();
		List<UserReport> issueList = new ArrayList<>();
		HashMap<String, AggregateUserReport> sprintReport = new HashMap<>();
		Iterable<Issue> retrievedIssue = restClient.getSearchClient()
				.searchJql(" sprint = '" + sprint + "' AND project = '" + project + "'").claim().getIssues();
		Pattern pattern = Pattern.compile("\\[\".*\\[id=(.*),rapidViewId=(.*),.*,name=(.*),startDate=(.*),.*\\]");
		Matcher matcher = pattern
				.matcher(retrievedIssue.iterator().next().getFieldByName("Sprint").getValue().toString());
		if( matcher.find() )
		{
			sprintId = Integer.parseInt(matcher.group(1));
			rvId = Integer.parseInt(matcher.group(2));
		}
		for( Issue issue : retrievedIssue )
		{
			if( issue.getAssignee() != null )
			{
				assignee = issue.getAssignee().getName();
				if( sprintReport.get(assignee) == null )
				{
					HashMap<String, String> userParams = new HashMap<>();
					userParams.put("sprint", sprint);
					userParams.put("project", project);
					userParams.put("user", assignee);
					userParams.put("export", "false");
					aggregateUserReport = userReportService.getUserReport(userParams, restClient);
					sprintReport.put(assignee, aggregateUserReport);
					issueList.addAll(aggregateUserReport.getIssues());
				}
			}
		}
		UserReport userReport = new UserReport();
		userReport.setKey("Removed Issues");
		issueList.add(userReport);
		String filename = project + "_" + sprint + ".csv";
		filename = filename.replace(" ", "_");
		try
		{
			RemovedIssues removedIssues = RemovedIssues.get(jiraClient.getRestClient(), rvId, sprintId);
			for( SprintIssue issueValue : removedIssues.getPuntedIssues() )
			{
				Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
				userReport = new UserReport();
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
							userReport.setOriginalEstimateMinutes(
									issue.get().getTimeTracking().getOriginalEstimateMinutes());
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
						userReport.setSprint(sprint);
					}
					issueList.add(userReport);
				}
				catch( InterruptedException | ExecutionException e )
				{
					logger.error("Error:" + e.getMessage());
					throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
				}
			}
			userReport = new UserReport();
			userReport.setKey("Issues Added during Sprint");
			issueList.add(userReport);
			for( String issueValue : removedIssues.getIssuesAdded() )
			{
				Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue);
				userReport = new UserReport();
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
							userReport.setOriginalEstimateMinutes(
									issue.get().getTimeTracking().getOriginalEstimateMinutes());
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
							pattern = Pattern.compile("\\[\".*\\[.*,name=(.*),startDate=(.*),.*\\]");
							matcher = pattern.matcher(issue.get().getFieldByName("Sprint").getValue().toString());
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
				}
				catch( InterruptedException | ExecutionException e )
				{
					logger.error("Error:" + e.getMessage());
					throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
				}
			}
		}
		catch( JiraException e )
		{
			logger.error("Error:" + e.getMessage());
			throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
		}
		ConvertToCSV exportToCSV = new ConvertToCSV();
		exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, issueList);
		return env.getProperty("csv.aliaspath") + filename;
	}
}
