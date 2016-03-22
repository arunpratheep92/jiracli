package com.razorthink.jira.cli.rest;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.jira.cli.completeJiraReport.service.CompleteJiraReportService;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.login.service.LoginService;

@RestController
@RequestMapping( "/jiraReport" )
public class CompleteJiraReportRestService {

	@Autowired
	CompleteJiraReportService completeJiraReportService;
	@Autowired
	LoginService loginService;

	@RequestMapping( value = "/getCompleteReport", method = RequestMethod.POST )
	public String getBacklogReport( @RequestBody Map<String, String> params )
	{
		try
		{
			JiraRestClient restClient = loginService.getRestClient();
			return completeJiraReportService.getCompleteJiraReport(params, restClient);
		}
		catch( DataException e )
		{
			return e.getMessage();
		}
		catch( Exception e )
		{
			return e.getMessage();
		}
	}
}
