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

@Service
public class BacklogReportServiceImpl implements BacklogReportService {

	@Autowired
	private Environment env;
	private static final Logger logger = LoggerFactory.getLogger(BacklogReportServiceImpl.class);

	/* (non-Javadoc)
	 * @see com.razorthink.jira.cli.backlog.service.impl.BacklogReportService#getBacklogReport(java.util.Map, com.atlassian.jira.rest.client.api.JiraRestClient)
	 */
	@Override
	public List<UserReport> getBacklogReport( Map<String, String> params, JiraRestClient restClient )
	{
		logger.debug("getBacklogReport");
		String project = params.get("project");
		List<UserReport> issueList = new ArrayList<>();
		Iterable<Issue> retrievedIssue = restClient.getSearchClient().searchJql(
				"project = '" + project + "' AND sprint is EMPTY AND resolution = Unresolved and status != Closed")
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
		ConvertToCSV exportToCSV = new ConvertToCSV();
		exportToCSV.exportToCSV(env.getProperty("csv.filename") + project + "_backlog.csv", issueList);
		return issueList;
	}
}
