package com.razorthink.jira.cli.aggregeteprojectreport.service.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.util.concurrent.Promise;
import com.razorthink.jira.cli.aggregeteprojectreport.service.AggregateProjectReportService;
import com.razorthink.jira.cli.domain.AggregateProjectReport;
import com.razorthink.jira.cli.domain.RemovedIssues;
import com.razorthink.jira.cli.domain.SprintDetails;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.utils.ConvertToCSV;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;
import net.rcarz.jiraclient.greenhopper.RapidView;
import net.rcarz.jiraclient.greenhopper.Sprint;

/**
 * 
 * @author arun
 *
 */
@Service
public class AggregateProjectReportServiceImpl implements AggregateProjectReportService {

	@Autowired
	private Environment env;
	private static final Logger logger = LoggerFactory.getLogger(AggregateProjectReportServiceImpl.class);

	/**
	 * Generates an Aggregate report of the project specified in the argument
	 * 
	 * @param params contains
	 * <ul>
	 * <li><strong>project</strong> Name of the project 
	 * <li><strong>rapidview</strong> Name of the rapidView board for which report is to be generated
	 * </ul>
	 * 
	 * @param restClient It is used to make Rest calls to Jira to fetch sprint details
	 * @param jiraClient It is used to fetch removed issues and issues added during a sprint
	 * @param gh GreenHopper client used to fetch rapidView details
	 * @return Complete url of the Aggregate Project report generated
	 * 
	 * @throws DataException If some internal error occurs
	 */
	@Override
	public String getAggregateProjectReport( Map<String, String> params, JiraRestClient restClient,
			JiraClient jiraClient, GreenHopperClient gh )
	{

		logger.debug("getAggregateProjectReport");
		String project = params.get("project");
		String rapidViewName = params.get("rapidview");
		if( project == null || rapidViewName == null )
		{
			logger.error("Error: Missing required paramaters");
			throw new DataException(HttpStatus.BAD_REQUEST.toString(), "Missing required paramaters");
		}
		Integer estimatedHours = 0;
		Integer loggedHours = 0;
		Integer noEstimatesCount = 0;
		Integer totalEstimates = 0;
		Integer noDescriptionCount = 0;
		Integer issuesWithoutStory = 0;
		Integer totalTasks = 0;
		Boolean flag = true;
		int rvId = 0;
		int sprintId = 0;
		DateTime startDt = null;
		DateTime endDt = null;
		DateTime completeDate = null;
		Double accuracy = 0.0;
		AggregateProjectReport aggregateProjectReport = new AggregateProjectReport();
		List<SprintDetails> sprintDetailsList = new ArrayList<>();
		try
		{
			List<RapidView> rapidviewsLIst = gh.getRapidViews();
			for( RapidView rapidView : rapidviewsLIst )
			{
				if( rapidView.getName().equals(rapidViewName) )
				{
					flag = false;
					List<Sprint> sprintList = rapidView.getSprints();
					if( sprintList.size() > 0 )
					{
						aggregateProjectReport.setIs_Sprint_followed(true);
					}
					else
					{
						aggregateProjectReport.setIs_Sprint_followed(false);
					}
					for( Sprint sprint : sprintList )
					{
						SprintDetails sprintDetails = new SprintDetails();
						sprintDetails.setName(sprint.getName());
						completeDate = null;
						Iterable<Issue> retrievedIssue = restClient.getSearchClient()
								.searchJql(" sprint = " + sprint.getId() + " AND project = '" + project + "'", 1000, 0,
										null)
								.claim().getIssues();
						if( retrievedIssue.iterator().hasNext() )
						{
							Pattern pattern = Pattern.compile(
									"[\\[,]\".*?\\[.*?=(\\d+),.*?=(\\d+),.*?name=(.*?),.*?=(.*?),.*?=(.*?),.*?=(.*?),.*?]\"");
							Matcher matcher = pattern.matcher(
									retrievedIssue.iterator().next().getFieldByName("Sprint").getValue().toString());
							while( matcher.find() )
							{
								if( matcher.group(3).equals(sprint.getName()) )
								{
									startDt = new DateTime(matcher.group(4));
									endDt = new DateTime(matcher.group(5));
									if( !matcher.group(6).equals("<null>") )
									{
										completeDate = new DateTime(matcher.group(6));
									}
									sprintId = Integer.parseInt(matcher.group(1));
									rvId = Integer.parseInt(matcher.group(2));
								}
							}
							sprintDetails.setStartDate(startDt.toString("MM/dd/yyyy"));
							sprintDetails.setEndDate(endDt.toString("MM/dd/yyyy"));
							if( completeDate != null )
							{
								int days = Days.daysBetween(endDt, completeDate).getDays();
								if( days >= 1 )
								{
									sprintDetails.setDeliveryStatus("Delayed by " + days + " day");
									if( days == 1 )
									{
										sprintDetails.setDeliveryStatus("Delayed by " + days + " day");
									}
								}
								else
								{
									sprintDetails.setDeliveryStatus("Completed on time");
								}
							}
							else
							{
								sprintDetails.setDeliveryStatus("In Progress");
							}
							totalEstimates = 0;
							noEstimatesCount = 0;
							noDescriptionCount = 0;
							for( Issue issueValue : retrievedIssue )
							{
								totalEstimates++;
								Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
								if( issue.get().getIssueType().getName().equals("Task") )
								{
									totalTasks++;
									if( !issue.get().getIssueLinks().iterator().hasNext() )
									{
										issuesWithoutStory++;
									}
								}
								if( issue.get().getTimeTracking() != null )
								{
									if( issue.get().getTimeTracking().getOriginalEstimateMinutes() != null )
									{
										estimatedHours += issue.get().getTimeTracking().getOriginalEstimateMinutes();
									}
									else
									{
										noEstimatesCount++;
									}
									if( issue.get().getTimeTracking().getTimeSpentMinutes() != null )
									{
										loggedHours += issue.get().getTimeTracking().getTimeSpentMinutes();
									}
								}
								if( issue.get().getDescription() == null )
								{
									noDescriptionCount++;
								}
							}
							RemovedIssues removedIssues = RemovedIssues.get(jiraClient.getRestClient(), rvId, sprintId);
							Integer changed = removedIssues.getIssuesAdded().size()
									+ removedIssues.getPuntedIssues().size();
							sprintDetails.setSprintChanges(changed + "/" + totalEstimates);
							accuracy = ((estimatedHours * 1D) / loggedHours) * 100;
							sprintDetails.setEstimatedVsActualAccuracy(accuracy.intValue() + "%");
							sprintDetails.setEstimateProvidedStatus(
									(totalEstimates - noEstimatesCount) + "/" + totalEstimates);
							sprintDetails.setTaskDescription_Statistics(
									(totalEstimates - noDescriptionCount) + "/" + totalEstimates);
							sprintDetailsList.add(sprintDetails);
							aggregateProjectReport
									.setBacklogCount(rapidView.getBacklogData().getBacklogIssues().size());
							aggregateProjectReport.setSprintDetails(sprintDetailsList);
						}
						else
						{
							sprintDetailsList.add(sprintDetails);
						}
					}
				}
			}
			if( flag )
			{
				logger.error("Error:" + "Rapidview does not exist ");
				throw new DataException(HttpStatus.BAD_REQUEST.name(), "Invalid RapidView");
			}
			aggregateProjectReport.setIssuesWithoutStory(issuesWithoutStory);
		}
		catch( Exception e )
		{
			e.printStackTrace();
			logger.error("Error:" + e.getMessage());
			throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
		}
		String filename = project + "_aggregate_report.csv";
		filename = filename.replace(" ", "_");
		ConvertToCSV exportToCSV = new ConvertToCSV();
		exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, aggregateProjectReport.getSprintDetails());
		FileWriter fileWriter = null;
		try
		{
			fileWriter = new FileWriter(env.getProperty("csv.filename") + filename, true);
			fileWriter.write("Is Sprint followed?," + aggregateProjectReport.getIs_Sprint_followed() + "\n");
			fileWriter.write("Backlog Count," + aggregateProjectReport.getBacklogCount() + "\n");
			fileWriter
					.write("Issues without Story," + aggregateProjectReport.getIssuesWithoutStory() + "/" + totalTasks);
		}
		catch( IOException e )
		{
			logger.error("Error:" + e.getMessage());
			throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
		}
		finally
		{
			try
			{
				fileWriter.flush();
				fileWriter.close();
			}
			catch( IOException e )
			{
				logger.error("Error:" + e.getMessage());
				throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
			}
		}
		return env.getProperty("csv.aliaspath") + filename;
	}
}
