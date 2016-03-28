package com.razorthink.jira.cli.rest;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.jira.cli.advancedLogin.service.AdvancedLoginService;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.login.service.LoginService;
import com.razorthink.jira.cli.removedIssueReport.service.RemovedIssuesReportService;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;

@RestController
@RequestMapping( "/removedIssues" )
public class RemovedIssuesReportRestService {

	@Autowired
	RemovedIssuesReportService removedIssuesReportService;

	@Autowired
	AdvancedLoginService advancedLoginService;

	@Autowired
	LoginService loginService;

	@RequestMapping( value = "/getRemovedIssues", method = RequestMethod.POST )
	public String getRemovedIssues( @RequestBody Map<String, String> params )
	{
		try
		{
			JiraRestClient restClient = loginService.getRestClient();
			GreenHopperClient gh = advancedLoginService.getGreenHopperClient();
			JiraClient jiraClient = advancedLoginService.getJiraClient();
			return removedIssuesReportService.getRemovedIssues(params, restClient, jiraClient, gh);
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
