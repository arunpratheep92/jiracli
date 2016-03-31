package com.razorthink.jira.cli.rest;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.jira.cli.backlog.service.BacklogReportService;
import com.razorthink.jira.cli.login.service.LoginService;

@RestController
@RequestMapping( "/backlogReport" )
public class BacklogReportRestService {

	@Autowired
	BacklogReportService backlogReportService;

	@Autowired
	LoginService loginService;

	@RequestMapping( value = "/getBacklogReport", method = RequestMethod.POST )
	public String getBacklogReport( @RequestBody Map<String, String> params )
	{
		try
		{
			JiraRestClient restClient = loginService.getRestClient();
			return backlogReportService.getBacklogReport(params, restClient);
		}
		catch( Exception e )
		{
			return e.getMessage();
		}
	}
}
