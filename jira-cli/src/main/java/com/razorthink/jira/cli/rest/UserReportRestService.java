package com.razorthink.jira.cli.rest;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.jira.cli.domain.AggregateUserReport;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.login.service.LoginService;
import com.razorthink.jira.cli.userReport.service.UserReportService;

@RestController
@RequestMapping( "/userReport" )
public class UserReportRestService {

	@Autowired
	UserReportService userReporService;

	@Autowired
	LoginService loginService;

	@RequestMapping( value = "/getUserReport", method = RequestMethod.POST )
	public String getUserReport( @RequestBody Map<String, String> params )
	{
		try
		{
			JiraRestClient restClient = loginService.getRestClient();
			AggregateUserReport report = userReporService.getUserReport(params, restClient);
			return report.getFilepath();
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
