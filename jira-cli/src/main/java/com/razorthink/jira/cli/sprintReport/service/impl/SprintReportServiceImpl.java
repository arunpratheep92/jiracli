package com.razorthink.jira.cli.sprintReport.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.razorthink.jira.cli.domain.AggregateUserReport;
import com.razorthink.jira.cli.domain.UserReport;
import com.razorthink.jira.cli.sprintReport.service.SprintReportService;
import com.razorthink.jira.cli.userReport.service.UserReportService;
import com.razorthink.jira.cli.utils.ConvertToCSV;

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
	public HashMap<String, AggregateUserReport> getSprintReport( Map<String, String> params, JiraRestClient restClient )
	{
		logger.debug("getSprintReport");
		String sprint = params.get("sprint");
		String project = params.get("project");
		String assignee = null;
		AggregateUserReport aggregateUserReport = new AggregateUserReport();
		List<UserReport> issueList = new ArrayList<>();
		HashMap<String, AggregateUserReport> sprintReport = new HashMap<>();
		Iterable<Issue> retrievedIssue = restClient.getSearchClient()
				.searchJql(" sprint = '" + sprint + "' AND project = '" + project + "'").claim().getIssues();
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
		ConvertToCSV exportToCSV = new ConvertToCSV();
		exportToCSV.exportToCSV(env.getProperty("csv.filename") + project + "_" + sprint + ".csv", issueList);
		return sprintReport;
	}
}
