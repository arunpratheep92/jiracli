package com.razorthink.jira.cli.sprintreport.minimal.service.impl;

import java.text.DecimalFormat;
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
import com.razorthink.jira.cli.domain.RemovedIssues;
import com.razorthink.jira.cli.domain.SprintReport;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.sprintreport.minimal.service.SprintReportMinimalService;
import com.razorthink.jira.cli.utils.ConvertToCSV;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.greenhopper.SprintIssue;

@Service
public class SprintReportMinimalServiceImpl implements SprintReportMinimalService {

	@Autowired
	private Environment env;

	private static final Logger logger = LoggerFactory.getLogger(SprintReportMinimalServiceImpl.class);

	/**
	 * Generates a minimal report of the sprint specified in the argument including 
	 * issues removed from sprint and issues added during sprint
	 * 
	 * @param params contains
	 * <ul>
	 * <li><strong>project</strong> Name of the project 
	 * <li><strong>sprint</strong> Name of the sprint for which report is to be generated
	 * </ul>
	 * @param restClient It is used to make Rest calls to Jira to fetch sprint details
	 * @param jiraClient It is used to fetch removed issues and issues added during a sprint
	 * @return Complete url of the minimal sprint report generated
	 * 
	 * @throws DataException If some internal error occurs
	 */
	@Override
	public String getMininmalSprintReport( Map<String, String> params, JiraRestClient restClient,
			JiraClient jiraClient )
	{
		logger.debug("getMininmalSprintReport");
		String sprint = params.get("sprint");
		String project = params.get("project");
		int rvId = 0;
		int sprintId = 0;
		if( project == null || sprint == null )
		{
			logger.error("Error: Missing required paramaters");
			throw new DataException(HttpStatus.BAD_REQUEST.toString(), "Missing required paramaters");
		}
		List<SprintReport> sprintReportList = new ArrayList<>();
		SprintReport sprintReport = new SprintReport();
		Iterable<Issue> retrievedIssue = restClient.getSearchClient()
				.searchJql(" sprint = '" + sprint + "' AND project = '" + project + "'", 1000, 0, null).claim()
				.getIssues();
		Pattern pattern = Pattern.compile("\\[\".*\\[id=(.*),rapidViewId=(.*),.*,name=(.*),startDate=(.*),.*\\]");
		Matcher matcher = pattern
				.matcher(retrievedIssue.iterator().next().getFieldByName("Sprint").getValue().toString());
		if( matcher.find() )
		{
			sprintId = Integer.parseInt(matcher.group(1));
			rvId = Integer.parseInt(matcher.group(2));
		}
		for( Issue issueValue : retrievedIssue )
		{
			Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
			sprintReport = new SprintReport();
			try
			{
				sprintReport.setIssueKey(issue.get().getKey());
				sprintReport.setIssueType(issue.get().getIssueType().getName());
				sprintReport.setStatus(issue.get().getStatus().getName());
				sprintReport.setIssueSummary(issue.get().getSummary());
				if( issue.get().getAssignee() != null )
				{
					sprintReport.setAssignee(issue.get().getAssignee().getDisplayName());
				}
				else
				{
					sprintReport.setAssignee("unassigned");
				}
				if( issue.get().getTimeTracking() != null )
				{
					if( issue.get().getTimeTracking().getOriginalEstimateMinutes() != null )
					{
						sprintReport.setEstimatedHours(new DecimalFormat("##.##")
								.format(issue.get().getTimeTracking().getOriginalEstimateMinutes() / 60D));
					}
					else
					{
						sprintReport.setEstimatedHours("0");
					}
					if( issue.get().getTimeTracking().getTimeSpentMinutes() != null )
					{
						sprintReport.setLoggedHours(new DecimalFormat("##.##")
								.format(issue.get().getTimeTracking().getTimeSpentMinutes() / 60D));
					}
					else
					{
						sprintReport.setLoggedHours("0");
					}
				}
				sprintReportList.add(sprintReport);
			}
			catch( InterruptedException | ExecutionException e )
			{
				logger.error("Error:" + e.getMessage());
				throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
			}
		}
		for( int i = 0; i < 3; i++ )
		{
			sprintReport = new SprintReport();
			sprintReport.setAssignee(" ");
			sprintReport.setEstimatedHours(" ");
			sprintReport.setIssueKey(" ");
			sprintReport.setIssueSummary(" ");
			sprintReport.setIssueType(" ");
			sprintReport.setLoggedHours(" ");
			sprintReport.setStatus(" ");
			sprintReportList.add(sprintReport);
		}
		sprintReport = new SprintReport();
		sprintReport.setAssignee(" ");
		sprintReport.setEstimatedHours(" ");
		sprintReport.setIssueSummary(" ");
		sprintReport.setIssueType(" ");
		sprintReport.setLoggedHours(" ");
		sprintReport.setStatus(" ");
		sprintReport.setIssueKey("Removed Issues");
		sprintReportList.add(sprintReport);
		try
		{
			RemovedIssues removedIssues = RemovedIssues.get(jiraClient.getRestClient(), rvId, sprintId);
			for( SprintIssue issueValue : removedIssues.getPuntedIssues() )
			{
				Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
				sprintReport = new SprintReport();
				try
				{

					sprintReport.setIssueKey(issue.get().getKey());
					sprintReport.setIssueType(issue.get().getIssueType().getName());
					sprintReport.setStatus(issue.get().getStatus().getName());
					sprintReport.setIssueSummary(issue.get().getSummary());
					if( issue.get().getAssignee() != null )
					{
						sprintReport.setAssignee(issue.get().getAssignee().getDisplayName());
					}
					else
					{
						sprintReport.setAssignee("unassigned");
					}
					if( issue.get().getTimeTracking() != null )
					{
						if( issue.get().getTimeTracking().getOriginalEstimateMinutes() != null )
						{
							sprintReport.setEstimatedHours(new DecimalFormat("##.##")
									.format(issue.get().getTimeTracking().getOriginalEstimateMinutes() / 60D));
						}
						else
						{
							sprintReport.setEstimatedHours("0");
						}
						if( issue.get().getTimeTracking().getTimeSpentMinutes() != null )
						{
							sprintReport.setLoggedHours(new DecimalFormat("##.##")
									.format(issue.get().getTimeTracking().getTimeSpentMinutes() / 60D));
						}
						else
						{
							sprintReport.setLoggedHours("0");
						}
					}
					sprintReportList.add(sprintReport);
				}
				catch( InterruptedException | ExecutionException e )
				{
					logger.error("Error:" + e.getMessage());
					throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
				}
			}
			for( int i = 0; i < 3; i++ )
			{
				sprintReport = new SprintReport();
				sprintReport.setAssignee(" ");
				sprintReport.setEstimatedHours(" ");
				sprintReport.setIssueKey(" ");
				sprintReport.setIssueSummary(" ");
				sprintReport.setIssueType(" ");
				sprintReport.setLoggedHours(" ");
				sprintReport.setStatus(" ");
				sprintReportList.add(sprintReport);
			}
			sprintReport = new SprintReport();
			sprintReport.setIssueKey("Issues Added during Sprint");
			sprintReport.setAssignee(" ");
			sprintReport.setEstimatedHours(" ");
			sprintReport.setIssueSummary(" ");
			sprintReport.setIssueType(" ");
			sprintReport.setLoggedHours(" ");
			sprintReport.setStatus(" ");
			sprintReportList.add(sprintReport);
			for( String issueValue : removedIssues.getIssuesAdded() )
			{
				Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue);
				sprintReport = new SprintReport();
				try
				{

					sprintReport.setIssueKey(issue.get().getKey());
					sprintReport.setIssueType(issue.get().getIssueType().getName());
					sprintReport.setStatus(issue.get().getStatus().getName());
					sprintReport.setIssueSummary(issue.get().getSummary());
					if( issue.get().getAssignee() != null )
					{
						sprintReport.setAssignee(issue.get().getAssignee().getDisplayName());
					}
					else
					{
						sprintReport.setAssignee("unassigned");
					}
					if( issue.get().getTimeTracking() != null )
					{
						if( issue.get().getTimeTracking().getOriginalEstimateMinutes() != null )
						{
							sprintReport.setEstimatedHours(new DecimalFormat("##.##")
									.format(issue.get().getTimeTracking().getOriginalEstimateMinutes() / 60D));
						}
						else
						{
							sprintReport.setEstimatedHours("0");
						}
						if( issue.get().getTimeTracking().getTimeSpentMinutes() != null )
						{
							sprintReport.setLoggedHours(new DecimalFormat("##.##")
									.format(issue.get().getTimeTracking().getTimeSpentMinutes() / 60D));
						}
						else
						{
							sprintReport.setLoggedHours("0");
						}
					}
					sprintReportList.add(sprintReport);
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
		String filename = project + "_" + sprint + "_minimal_report.csv";
		filename = filename.replace(" ", "_");
		ConvertToCSV exportToCSV = new ConvertToCSV();
		exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, sprintReportList);
		return env.getProperty("csv.aliaspath") + filename;
	}
}
