package com.razorthink.jira.cli.completeJiraReport.service.impl;

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
import com.razorthink.jira.cli.completeJiraReport.service.CompleteJiraReportService;
import com.razorthink.jira.cli.domain.JiraReportIssue;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.utils.ConvertToCSV;
import com.razorthink.utils.cmutils.NullEmptyUtils;

@Service
public class CompleteJiraReportServiceImpl implements CompleteJiraReportService {

	@Autowired
	private Environment env;
	private static final Logger logger = LoggerFactory.getLogger(CompleteJiraReportServiceImpl.class);

	/* (non-Javadoc)
	 * @see com.razorthink.jira.cli.completeJiraReport.service.impl.CompleteJiraReportService#getCompleteJiraReport(java.lang.String, com.atlassian.jira.rest.client.api.JiraRestClient)
	 */
	@Override
	public String getCompleteJiraReport( Map<String, String> params, JiraRestClient restClient )
	{
		logger.debug("getCompleteJiraReport");
		String jql = null;
		Integer maxResults = null;
		Integer startAt = null;
		try
		{
			jql = params.get("jql");
			maxResults = Integer.parseInt(params.get("maxResults"));
			startAt = Integer.parseInt(params.get("startAt"));
		}
		catch( Exception e )
		{
			logger.error("Error: Missing required paramaters");
			throw new DataException(HttpStatus.BAD_REQUEST.toString(), "Missing required paramaters");
		}
		List<JiraReportIssue> report = new ArrayList<JiraReportIssue>();
		String project = "DefaultProject";
		Iterable<Issue> retrievedIssue = restClient.getSearchClient().searchJql(jql, maxResults, startAt, null).claim()
				.getIssues();
		for( Issue issueValue : retrievedIssue )
		{
			Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
			JiraReportIssue JiraReportIssue = new JiraReportIssue();
			try
			{
				StringBuilder sprint = new StringBuilder("");
				JiraReportIssue.setKey(issue.get().getKey());
				JiraReportIssue.setStatus(issue.get().getStatus().getName());
				JiraReportIssue.setIssueType(issue.get().getIssueType().getName());
				JiraReportIssue.setProject(issue.get().getProject().getName());
				project = JiraReportIssue.getProject();
				JiraReportIssue.setSummary(issue.get().getSummary());
				JiraReportIssue.setReporter(issue.get().getReporter().getName());
				JiraReportIssue.setReporterDiplayName(issue.get().getReporter().getDisplayName());
				if( issue.get().getAssignee() != null )
				{
					JiraReportIssue.setAssignee(issue.get().getAssignee().getName());
					JiraReportIssue.setAssigneeDiplayName(issue.get().getAssignee().getDisplayName());
				}
				else
				{
					JiraReportIssue.setAssignee("Unassigned");
				}
				JiraReportIssue.setCreationDate(issue.get().getCreationDate().toString("MM/dd/yy HH:mm:ss"));
				if( issue.get().getUpdateDate() != null )
				{
					JiraReportIssue.setUpdateDate(issue.get().getUpdateDate().toString("MM/dd/yy HH:mm:ss"));
				}
				else
				{
					JiraReportIssue.setUpdateDate("null");
				}
				if( issue.get().getPriority() != null )
				{
					JiraReportIssue.setPriority(issue.get().getPriority().getName());
				}
				else
				{
					JiraReportIssue.setPriority("null");
				}
				if( issue.get().getTimeTracking() != null )
				{
					if( issue.get().getTimeTracking().getOriginalEstimateMinutes() != null )
					{
						JiraReportIssue
								.setOriginalEstimateMinutes(issue.get().getTimeTracking().getOriginalEstimateMinutes());
					}
					else
					{
						JiraReportIssue.setOriginalEstimateMinutes(0);
					}
					if( issue.get().getTimeTracking().getTimeSpentMinutes() != null )
					{
						JiraReportIssue.setTimeSpentMinutes(issue.get().getTimeTracking().getTimeSpentMinutes());
					}
					else
					{
						JiraReportIssue.setTimeSpentMinutes(0);
					}
					if( issue.get().getTimeTracking().getRemainingEstimateMinutes() != null )
					{
						JiraReportIssue.setRemainingEstimateMinutes(
								issue.get().getTimeTracking().getRemainingEstimateMinutes());
					}
					else
					{
						JiraReportIssue.setRemainingEstimateMinutes(0);
					}
				}
				if( !NullEmptyUtils.isNullorEmpty((List<?>) issue.get().getFields()) )
				{
					if( issue.get().getFieldByName("Epic Link") != null
							&& issue.get().getFieldByName("Epic Link").getValue() != null )
					{
						JiraReportIssue.setEpicKey(issue.get().getFieldByName("Epic Link").getValue().toString());
					}
					else
					{
						JiraReportIssue.setEpicKey("null");
					}
					if( issue.get().getFieldByName("Sprint") != null
							&& issue.get().getFieldByName("Sprint").getValue() != null )
					{
						Pattern pattern = Pattern.compile("name=(.*?),startDate=.*?");
						Matcher matcher = pattern.matcher(issue.get().getFieldByName("Sprint").getValue().toString());
						while( matcher.find() )
						{
							sprint.append(matcher.group(1)).append(" ");
						}
						JiraReportIssue.setSprint(sprint.toString());
					}
					else
					{
						JiraReportIssue.setSprint("null");
					}
				}
				report.add(JiraReportIssue);
			}
			catch( InterruptedException | ExecutionException e )
			{
				logger.error("Error:" + e.getMessage());
				throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
			}
		}
		String filename = project + "_complete_dump_" + startAt + "_to_" + maxResults + ".csv";
		filename = filename.replace(" ", "_");
		ConvertToCSV exportToCSV = new ConvertToCSV();
		exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, report);
		return env.getProperty("csv.aliaspath") + filename;
	}

}
