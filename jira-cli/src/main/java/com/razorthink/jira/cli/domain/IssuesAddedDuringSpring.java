package com.razorthink.jira.cli.domain;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.RestClient;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

public class IssuesAddedDuringSpring {

	static final String RESOURCE_URI = "/rest/greenhopper/1.0/";
	private RestClient restclient = null;
	private Set addedIssues = null;

	protected IssuesAddedDuringSpring( RestClient restclient, JSONObject json )
	{
		this.restclient = restclient;
		if( json != null )
			deserialise(json);
	}

	@SuppressWarnings( "rawtypes" )
	private void deserialise( JSONObject json )
	{

		Map map = json;
		Map m1 = (Map) map.get("issueKeysAddedDuringSprint");
		addedIssues = m1.keySet();
	}

	@SuppressWarnings( "serial" )
	public static IssuesAddedDuringSpring get( RestClient restclient, int rvId, int sprintId ) throws JiraException
	{

		JSON result = null;
		try
		{

			URI reporturi = restclient.buildURI(RESOURCE_URI + "rapid/charts/sprintreport",
					new HashMap<String, String>() {

						{
							put("rapidViewId", Integer.toString(rvId));
							put("sprintId", Integer.toString(sprintId));
						}
					});
			result = restclient.get(reporturi);
		}
		catch( Exception ex )
		{
			throw new JiraException("Failed to retrieve sprint report", ex);
		}

		if( !(result instanceof JSONObject) )
			throw new JiraException("JSON payload is malformed");

		JSONObject jo = (JSONObject) result;

		if( !jo.containsKey("contents") || !(jo.get("contents") instanceof JSONObject) )
			throw new JiraException("Sprint report content is malformed");

		return new IssuesAddedDuringSpring(restclient, (JSONObject) jo.get("contents"));
	}

	public Set getAddedIssues()
	{
		return addedIssues;
	}

}
