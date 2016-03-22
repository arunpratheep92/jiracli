package com.razorthink.jira.cli.rest;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.login.service.LoginService;
import com.razorthink.jira.cli.sprintReport.service.SprintReportService;

@RestController
@RequestMapping( "/sprintReport" )
public class SprintReportRestService {

	@Autowired
	SprintReportService sprintReportService;

	@Autowired
	LoginService loginService;

	@RequestMapping( value = "/getSprintReport", method = RequestMethod.POST )
	public String getSprintReport( @RequestBody Map<String, String> params )
	{
		try
		{
			JiraRestClient restClient = loginService.getRestClient();
			return sprintReportService.getSprintReport(params, restClient);
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
