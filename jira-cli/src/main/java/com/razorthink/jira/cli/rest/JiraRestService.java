package com.razorthink.jira.cli.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.razorthink.jira.cli.domain.JiraIssue;
import com.razorthink.jira.cli.service.JiraService;
import com.razorthink.utils.cmutils.JSONUtils;
import com.razorthink.utils.cmutils.exception.UtilsException;

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
		commands = commands.replaceAll("\\s+", " ");
		commands = commands.trim();
		Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
		Matcher regexMatcher = regex.matcher(commands);
		List<String> commandToken = new ArrayList<>();
		while (regexMatcher.find()) {
			if (regexMatcher.group(1) != null) {
				commandToken.add((regexMatcher.group(1)));
			} else if (regexMatcher.group(2) != null) {
				commandToken.add(regexMatcher.group(2));
			} else {
				commandToken.add(regexMatcher.group());
			}
		}
		System.out.println("\n\n Tokenss=" + commandToken);
		if (!jiraService.validate(commandToken)) {
			return "Invalid Syntax";
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
					List<JiraIssue> jqlResult = jiraService.getAllIssues(commandToken);
					String result = JSONUtils.toJson(jqlResult);
					return result;
				} catch (Exception e) {
					return e.getMessage();
				}
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
			case "-getDueDate":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String dueDate = jiraService.getDueDate(issue, project);
					return dueDate;
				} catch (Exception e) {
					return e.getMessage();
				}
			case "-getPriority":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String priority = jiraService.getPriority(issue, project);
					return priority;
				} catch (Exception e) {
					return e.getMessage();
				}
			case "-getVotes":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String votes = jiraService.getVotes(issue, project);
					return votes;
				} catch (Exception e) {
					return e.getMessage();
				}
			case "-getFixVersions":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String fixversions = jiraService.getFixVersions(issue, project);
					return fixversions;
				} catch (Exception e) {
					return e.getMessage();
				}
			case "-getComments":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String comments = jiraService.getComments(issue, project);
					return comments;
				} catch (Exception e) {
					return e.getMessage();
				}
			case "-getWatchers":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String watchers = jiraService.getWatchers(issue, project);
					return watchers;
				} catch (Exception e) {
					return e.getMessage();
				}
			case "-getLabel":
				iterator.next();
				issue = iterator.next();
				iterator.next();
				project = iterator.next();
				try {
					String labels = jiraService.getLabels(issue, project);
					return labels;
				} catch (Exception e) {
					return e.getMessage();
				}

			case "-jql":
				StringBuilder jqlValue = new StringBuilder("");
				while (iterator.hasNext()) {
					jqlValue.append(iterator.next()).append(" ");
				}
				List<JiraIssue> jqlResult = jiraService.getJqlResult(jqlValue.toString());
				try {
					String result = JSONUtils.toJson(jqlResult);
					return result;
				} catch (UtilsException e) {
					return e.getMessage();
				}
			case "--help":
				return jiraService.getHelp();
			}
		}
		return "valid";
	}
}
