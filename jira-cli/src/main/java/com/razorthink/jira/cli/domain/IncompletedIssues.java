package com.razorthink.jira.cli.domain;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.RestClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperField;
import net.rcarz.jiraclient.greenhopper.SprintIssue;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

public class IncompletedIssues {

	static final String RESOURCE_URI = "/rest/greenhopper/1.0/";
	private RestClient restclient = null;
	private List<SprintIssue> incompleteIssues = null;

	protected IncompletedIssues( RestClient restclient, JSONObject json )
	{
		this.restclient = restclient;
		if( json != null )
			deserialise(json);
	}

	@SuppressWarnings( "rawtypes" )
	private void deserialise( JSONObject json )
	{
		Map map = json;
		incompleteIssues = GreenHopperField.getResourceArray(SprintIssue.class,
				map.get("issuesNotCompletedInCurrentSprint"), restclient);
	}

	@SuppressWarnings( "serial" )
	public static IncompletedIssues get( RestClient restclient, int rvId, int sprintId ) throws JiraException
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

		return new IncompletedIssues(restclient, (JSONObject) jo.get("contents"));
	}

	public List<SprintIssue> getIncompleteIssues()
	{
		return incompleteIssues;
	}

}
