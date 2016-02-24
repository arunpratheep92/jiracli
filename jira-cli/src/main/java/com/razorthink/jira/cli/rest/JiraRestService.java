package com.razorthink.jira.cli.rest;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.razorthink.jira.cli.service.JiraService;

@RestController
@RequestMapping("/jiraServices")
@Component
public class JiraRestService {

	@Autowired
	JiraService jiraService;

	@Consumes({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
	@RequestMapping(value = "/commands", method = RequestMethod.POST)
	public String jiraService(@RequestBody String commands) {
		commands = commands.trim();
		System.out.println("\n\nCommands==" + commands);
		commands = commands.replaceAll("\\s+", " ");
		String[] commandTokens = commands.split(" ");
		if (!jiraService.validate(commandTokens)) {
			return "Invalid Syntax";
		}
		List<String> commandToken = Arrays.asList(commandTokens);
		for (String token : commandToken) {
			System.out.println("\nToken== " + token);
		}
		ListIterator<String> iterator = commandToken.listIterator();
		while (iterator.hasNext()) {
			switch (iterator.next()) {
			case "jira":
				break;
			case "-u": {
				String username = iterator.next();
				iterator.next();
				String password = iterator.next();
				iterator.next();
				String url = iterator.next();
				try {
					if (jiraService.login(username, password, url)) {
						if (!iterator.hasNext()) {
							return "login success";
						}
					}
				} catch (Exception e) {
					return e.getMessage();
				}
				break;
			}
			case "-getProjects": {
				try {
					List<BasicProject> projects = jiraService.getAllProjects();
					return projects.toString();
				} catch (Exception e) {
					return e.getMessage();
				}
			}
			case "-getIssues": {
				try {
					jiraService.getAllProjects();
				} catch (Exception e) {
					return e.getMessage();
				}
				break;

			}
			case "-getStatus": 
				iterator.next();
				String issue = iterator.next();
				iterator.next();
				String project = iterator.next();
				try {
					String status = jiraService.getStatus(issue, project);
					return status;
				} catch (Exception e) {
					return e.getMessage();
				}
			case "-getIssueType":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String status = jiraService.getIssueType(issue, project);
					return status;
				} catch (Exception e) {
					return e.getMessage();
				}
			case "-getComponents":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String status = jiraService.getComponents(issue, project);
					return status;
				} catch (Exception e) {
					return e.getMessage();
				}
			case "-getDescription":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String description = jiraService.getDesription(issue, project);
					return description;
				} catch (Exception e) {
					return e.getMessage();
				}
			case "-getReporter":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String reporter = jiraService.getReporter(issue, project);
					return reporter;
				} catch (Exception e) {
					return e.getMessage();
				}
			case "-getAssignee":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String assignee = jiraService.getAssignee(issue, project);
					return assignee;
				} catch (Exception e) {
					return e.getMessage();
				}
			case "-getResolution":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String resolution = jiraService.getResolution(issue, project);
					return resolution;
				} catch (Exception e) {
					return e.getMessage();
				}
			case "-getCreationDate":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String createdDate = jiraService.getCreationDate(issue, project);
					return createdDate;
				} catch (Exception e) {
					return e.getMessage();
				}
			case "-getUpdateDate":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String updateDate = jiraService.getUpdateDate(issue, project);
					return updateDate;
				} catch (Exception e) {
					return e.getMessage();
				}
			/*case "-getDueDate":
			case "-getPriority":
			case "-getVotes":
			case "-getFixVersions":
			case "-getComments":
			case "-getWatchers":
			case "-getLabel":
				iterator.next();
				Method method = JiraServiceImpl.class.getMethod(iterator.previous());
				String value = (String) method.invoke(jiraService);
				if (!iterator.hasNext() || (iterator.hasNext() && !iterator.next().equals("--issue"))) {
					return false;
				}
				if (!iterator.hasNext() || (iterator.hasNext() && jiraCommands.contains(iterator.next()))) {
					return false;
				}
				if (!iterator.hasNext() || (iterator.hasNext() && !iterator.next().equals("--project"))) {
					return false;
				}
				if (!iterator.hasNext() || (iterator.hasNext() && jiraCommands.contains(iterator.next()))) {
					return false;
				}
				break;
			case "-jql":
				while (iterator.hasNext()) {
					String jqlValue = iterator.next();
					if (jiraCommands.contains(jqlValue) || followUpCommands.contains(jqlValue)) {
						return false;
					}
				}

			default:
				return false;
*/			}
			// jiraService.jqlBuilder(commandTokens);
		}
		return "valid";
	}
}
