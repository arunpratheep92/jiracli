package com.razorthink.jira.cli.login.service.impl;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.login.service.LoginService;
import com.razorthink.utils.jira.api.JiraService;

/**
 * 
 * @author arun
 *
 */
@Service
public class LoginServiceImpl implements LoginService {

	private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);
	private static JiraRestClient restClient;

	/* (non-Javadoc)
	 * @see com.razorthink.jira.cli.login.service.impl.LoginService#authorize(java.util.Map)
	 */
	@Override
	public JiraRestClient authorize( Map<String, String> params )
	{
		logger.debug("authorizing");

		JiraService js = new JiraService();
		String username = params.get("username");
		String password = params.get("password");
		String url = params.get("url");
		try
		{
			restClient = js.authorize(url, username, password);
			restClient.getProjectClient().getAllProjects().claim();
			return restClient;
		}
		catch( Exception e )
		{
			logger.error(e.getMessage());
			throw new DataException(HttpStatus.BAD_REQUEST.name(), "Could not login");
		}
	}

	/* (non-Javadoc)
	 * @see com.razorthink.jira.cli.login.service.impl.LoginService#getRestClient()
	 */
	@Override
	public JiraRestClient getRestClient()
	{
		try
		{
			restClient.getProjectClient().getAllProjects().claim();
			return restClient;
		}
		catch( Exception e )
		{
			logger.error(e.getMessage());
			throw new DataException(HttpStatus.UNAUTHORIZED.name(), "User not logged in");
		}
	}
}
