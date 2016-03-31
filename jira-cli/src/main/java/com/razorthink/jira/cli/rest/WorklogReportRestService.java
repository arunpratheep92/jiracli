package com.razorthink.jira.cli.rest;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.jira.cli.login.service.LoginService;
import com.razorthink.jira.cli.worklogReport.service.WorklogReportService;

@RestController
@RequestMapping( "/worklogReport" )
public class WorklogReportRestService {

	@Autowired
	WorklogReportService worklogReportService;

	@Autowired
	LoginService loginService;

	@RequestMapping( value = "/getWorklogReport", method = RequestMethod.POST )
	public String getTimesheetReport( @RequestBody Map<String, String> params )
	{
		try
		{
			JiraRestClient restClient = loginService.getRestClient();
			return worklogReportService.getWorklogReport(params, restClient);
		}
		catch( Exception e )
		{
			return e.getMessage();
		}
	}
}
