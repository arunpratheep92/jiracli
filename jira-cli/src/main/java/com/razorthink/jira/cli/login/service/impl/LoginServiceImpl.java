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

	/**
	 * Authorize is used to authorize JiraRestClient using the credentials provided.
	 * It can be used to perform operations in Jira by means of Rest calls. 
	 * 
	 * @param params Contains username,password and url to authorize the user to Jira
	 * @return JiraRestClient object
	 */
	@Override
	public JiraRestClient authorize( Map<String, String> params )
	{
		logger.debug("authorizing");

		JiraService js = new JiraService();
		String username = params.get("username");
		String password = params.get("password");
		String url = params.get("url");
		if( username == null || password == null || url == null )
		{
			throw new DataException(HttpStatus.BAD_REQUEST.name(), "Parameters cannot be null");
		}
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

	/**
	 * Returns a JiraRestClient Object which is used to fetch details from Jira.
	 * 
	 * @return JiraRestClient object
	 * 
	 * @throws DataException if user is not logged in
	 */
	@Override
	public JiraRestClient getRestClient()
	{
		if( restClient == null )
		{
			throw new DataException(HttpStatus.UNAUTHORIZED.name(), "User not logged in");
		}
		return restClient;
	}
}
