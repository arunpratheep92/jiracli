package com.razorthink.jira.cli.rest;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.jira.cli.domain.AggregateUserReport;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.login.service.LoginService;
import com.razorthink.jira.cli.sprintReport.service.SprintReportService;
import com.razorthink.jira.cli.utils.Response;

@RestController
@RequestMapping( "/sprintReport" )
public class SprintReportRestService {

	@Autowired
	SprintReportService sprintReportService;

	@Autowired
	LoginService loginService;

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	@RequestMapping( value = "/getSprintReport", method = RequestMethod.POST )
	public ResponseEntity<Response> getSprintReport( @RequestBody Map<String, String> params )
	{
		Response response = new Response();
		try
		{
			JiraRestClient restClient = loginService.getRestClient();
			HashMap<String, AggregateUserReport> report = sprintReportService.getSprintReport(params, restClient);
			response.setErrorCode(null);
			response.setErrorMessage(null);
			response.setObject(report);
			return new ResponseEntity(response, HttpStatus.OK);
		}
		catch( DataException e )
		{
			response.setErrorCode(HttpStatus.UNAUTHORIZED.toString());
			response.setErrorMessage(e.getMessage());
			response.setObject(null);
			return new ResponseEntity(response, HttpStatus.UNAUTHORIZED);
		}
		catch( Exception e )
		{
			response.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.toString());
			response.setErrorMessage(e.getMessage());
			response.setObject(null);
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
