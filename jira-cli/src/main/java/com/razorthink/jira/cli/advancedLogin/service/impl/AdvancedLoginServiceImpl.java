package com.razorthink.jira.cli.advancedLogin.service.impl;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.razorthink.jira.cli.advancedLogin.service.AdvancedLoginService;
import com.razorthink.jira.cli.exception.DataException;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;

/**
 * 
 * @author arun
 *
 */
@Service
public class AdvancedLoginServiceImpl implements AdvancedLoginService {

	private static final Logger logger = LoggerFactory.getLogger(AdvancedLoginServiceImpl.class);
	private static JiraClient jira;
	private static GreenHopperClient gh;

	/**
	 * AdvancedLogin Authorize is used to authorize JiraClient and GreenHopperClient
	 * using the credentials provided.They can be used to perform operations in Jira
	 * by means of Rest calls. 
	 * 
	 * @param params Contains username,password and url to authorize the user to Jira
	 */
	@Override
	public void authorize( Map<String, String> params )
	{
		logger.debug("advance authorizing");
		String username = params.get("username");
		String password = params.get("password");
		String url = params.get("url");
		BasicCredentials creds = new BasicCredentials(username, password);
		jira = new JiraClient(url, creds);
		gh = new GreenHopperClient(jira);
	}

	/**
	 * Returns a JiraClient Object which is used to fetch details from Jira.
	 * 
	 * @return JiraClient object
	 * 
	 * @throws DataException if user is not logged in
	 */
	@Override
	public JiraClient getJiraClient()
	{
		if( jira == null )
		{
			throw new DataException(HttpStatus.UNAUTHORIZED.name(), "User not logged in");
		}
		return jira;
	}

	/**
	 * Returns a GreenHopperClient Object which is used to fetch details from Jira.
	 * 
	 * @return GreenHopperClient object
	 * 
	 * @throws DataException if user is not logged in
	 */
	@Override
	public GreenHopperClient getGreenHopperClient()
	{
		if( gh == null )
		{
			throw new DataException(HttpStatus.UNAUTHORIZED.name(), "User not logged in");
		}
		return gh;
	}
}
