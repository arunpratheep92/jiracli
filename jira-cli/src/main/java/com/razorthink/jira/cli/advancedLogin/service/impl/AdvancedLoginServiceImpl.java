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

@Service
public class AdvancedLoginServiceImpl implements AdvancedLoginService {

	private static final Logger logger = LoggerFactory.getLogger(AdvancedLoginServiceImpl.class);
	private static JiraClient jira;
	private static GreenHopperClient gh;

	/* (non-Javadoc)
	 * @see com.razorthink.jira.cli.advancedLogin.service.impl.AdvancedLoginService#authorize(java.util.Map)
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

	/* (non-Javadoc)
	 * @see com.razorthink.jira.cli.advancedLogin.service.impl.AdvancedLoginService#getJiraClient()
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

	/* (non-Javadoc)
	 * @see com.razorthink.jira.cli.advancedLogin.service.impl.AdvancedLoginService#getGreenHopperClient()
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
