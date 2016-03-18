package com.razorthink.jira.cli.rest;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.login.service.LoginService;
import com.razorthink.jira.cli.utils.Response;

@RestController
@RequestMapping( "/login" )
public class LoginRestService {

	@Autowired
	LoginService loginService;

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	@RequestMapping( value = "/authorize", method = RequestMethod.POST )
	public ResponseEntity<Response> authorize( @RequestBody Map<String, String> params )
	{
		Response response = new Response();
		try
		{

			loginService.authorize(params);
			response.setErrorCode(null);
			response.setErrorMessage(null);
			response.setObject("Login Success");
			return new ResponseEntity(response, HttpStatus.OK);
		}
		catch( DataException e )
		{
			response.setErrorCode(HttpStatus.UNAUTHORIZED.toString());
			response.setErrorMessage("Invalid Credentials");
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
