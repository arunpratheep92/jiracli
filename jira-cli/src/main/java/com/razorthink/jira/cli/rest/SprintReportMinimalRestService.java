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
import com.razorthink.jira.cli.sprintreport.minimal.service.SprintReportMinimalService;
import net.rcarz.jiraclient.JiraClient;

@RestController
@RequestMapping( "/sprintReport" )
public class SprintReportMinimalRestService {

	@Autowired
	SprintReportMinimalService sprintReportMinimalService;

	@Autowired
	LoginService loginService;

	@Autowired
	AdvancedLoginService advancedLoginService;

	@RequestMapping( value = "/getMinimalSprintReport", method = RequestMethod.POST )
	public String getMinimalSprintReport( @RequestBody Map<String, String> params )
	{
		try
		{
			JiraRestClient restClient = loginService.getRestClient();
			JiraClient jiraClient = advancedLoginService.getJiraClient();
			return sprintReportMinimalService.getMininmalSprintReport(params, restClient, jiraClient);
		}
		catch( Exception e )
		{
			return e.getMessage();
		}
	}
}
