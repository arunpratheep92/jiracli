package com.razorthink.jira.cli.rest;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jiraServices")
@Component
public class JiraRestService {

	static List<String> jiraCommands = Arrays.asList("-u", "-p", "-url", "-getProjects", "-getIssues", "-getStatus",
			"-getIssueType", "-getComponents", "-getDescription", "-getReporter", "-getAssignee", "-getResolution",
			"-getCreationDate", "-getUpdateDate", "-getDueDate", "-getPriority", "-getVotes", "-getFixVersions",
			"-getComments", "-getWatchers", "-getLabels");

	static List<String> followUpCommands = Arrays.asList("--issue", "--project");

	@Consumes({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
	@RequestMapping(value = "/commands", method = RequestMethod.POST)
	public String jiraService(@RequestBody String commands) {
		commands=commands.trim();
		System.out.println("\n\nCommands==" + commands);
		commands = commands.replaceAll("\\s+", " ");
		String[] commandTokens = commands.split(" ");
		if(!validate(commandTokens)){
			return "Invalid Syntax";
		}
		return null;
	}

	public Boolean validate(String[] commandTokens) {
		System.out.println("command 0"+commandTokens[0]);
		if (!commandTokens[0].equals("jira")) {
			return false;
		}
		for (String commandToken : commandTokens) {
			if(jiraCommands.contains(commandToken)){
				switch(commandToken){
				case "-u":
					
				}
				System.out.println("here");
			}
			System.out.println(commandToken);
		}
		return true;
	}

}
