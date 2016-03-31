package com.razorthink.jira.cli.rest;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.jira.cli.advancedLogin.service.AdvancedLoginService;
import com.razorthink.jira.cli.login.service.LoginService;
import com.razorthink.jira.cli.sprintRetrospectionReport.service.SprintRetrospectionReportService;
import net.rcarz.jiraclient.JiraClient;

@RestController
@RequestMapping( "/sprintRetrospectionReport" )
public class SprintRetrospectionRestService {

	@Autowired
	SprintRetrospectionReportService sprintRetrospectionReportService;

	@Autowired
	LoginService loginService;

	@Autowired
	AdvancedLoginService advancedLoginService;

	@RequestMapping( value = "/getSprintRetrospectionReport", method = RequestMethod.POST )
	public String getSprintReport( @RequestBody Map<String, String> params )
	{
		try
		{
			JiraRestClient restClient = loginService.getRestClient();
			JiraClient jiraClient = advancedLoginService.getJiraClient();
			return sprintRetrospectionReportService.getSprintRetrospectionReport(params, restClient, jiraClient);
		}
		catch( Exception e )
		{
			return e.getMessage();
		}
	}
}
