package com.razorthink.jira.cli.rest;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.razorthink.jira.cli.advancedLogin.service.AdvancedLoginService;
import com.razorthink.jira.cli.login.service.LoginService;

@RestController
@RequestMapping( "/login" )
public class LoginRestService {

	@Autowired
	LoginService loginService;

	@Autowired
	AdvancedLoginService advancedLoginService;

	@RequestMapping( value = "/authorize", method = RequestMethod.POST )
	public String authorize( @RequestBody Map<String, String> params )
	{
		try
		{
			loginService.authorize(params);
			advancedLoginService.authorize(params);
			return "Login Success";
		}
		catch( Exception e )
		{
			return "Login Failed";
		}
	}
}
