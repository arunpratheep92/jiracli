package com.razorthink.jira.cli.advancedLogin.service;

import java.util.Map;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;

public interface AdvancedLoginService {

	void authorize( Map<String, String> params );

	JiraClient getJiraClient();

	GreenHopperClient getGreenHopperClient();

}