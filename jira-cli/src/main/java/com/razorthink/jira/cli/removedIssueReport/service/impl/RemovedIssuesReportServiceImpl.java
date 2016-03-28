package com.razorthink.jira.cli.removedIssueReport.service.impl;

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
import com.razorthink.jira.cli.domain.UserReport;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.removedIssueReport.service.RemovedIssuesReportService;
import com.razorthink.jira.cli.utils.ConvertToCSV;
import com.razorthink.utils.cmutils.NullEmptyUtils;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;
import net.rcarz.jiraclient.greenhopper.SprintIssue;

@Service
public class RemovedIssuesReportServiceImpl implements RemovedIssuesReportService {

	@Autowired
	private Environment env;
	private static final Logger logger = LoggerFactory.getLogger(RemovedIssuesReportServiceImpl.class);

	/* (non-Javadoc)
	 * @see com.razorthink.jira.cli.removedIssueReport.service.impl.RemovedIssuesReportService#getRemovedIssues(java.util.Map, com.atlassian.jira.rest.client.api.JiraRestClient, net.rcarz.jiraclient.JiraClient, net.rcarz.jiraclient.greenhopper.GreenHopperClient)
	 */
	@Override
	public String getRemovedIssues( Map<String, String> params, JiraRestClient restClient, JiraClient jiraClient,
			GreenHopperClient gh )
	{
		logger.debug("getRemovedIssues");

		List<UserReport> issueList = new ArrayList<>();
		int rvId = Integer.parseInt(params.get("rapidviewId"));
		int sprintId = Integer.parseInt(params.get("sprintId"));
		try
		{
			RemovedIssues removedIssues = RemovedIssues.get(jiraClient.getRestClient(), rvId, sprintId);
			for( SprintIssue issueValue : removedIssues.getPuntedIssues() )
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
							Pattern pattern = Pattern.compile("\\[\".*\\[.*,name=(.*),startDate=(.*),.*\\]");
							Matcher matcher = pattern
									.matcher(issue.get().getFieldByName("Sprint").getValue().toString());
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
		String filename = "rapidview_" + rvId + "_sprint_" + sprintId + "_removedIssues.csv";
		filename = filename.replace(" ", "_");
		ConvertToCSV exportToCSV = new ConvertToCSV();
		exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, issueList);
		return env.getProperty("csv.aliaspath") + filename;
	}
}
