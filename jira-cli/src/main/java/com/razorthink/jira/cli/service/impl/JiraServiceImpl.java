package com.razorthink.jira.cli.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.razorthink.jira.cli.domain.JiraIssue;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.service.JiraService;

import net.rcarz.jiraclient.JiraException;

@Service
public class JiraServiceImpl implements JiraService {
	static List<String> jiraCommands = Arrays.asList("-u", "-p", "-url", "-getProjects", "-getIssues", "-getStatus",
			"-getIssueType", "-getComponents", "-getDescription", "-getReporter", "-getAssignee", "-getResolution",
			"-getCreationDate", "-getUpdateDate", "-getDueDate", "-getPriority", "-getVotes", "-getFixVersions",
			"-getComments", "-getWatchers", "-getLabels");

	static List<String> followUpCommands = Arrays.asList("--status", "--issuetype", "--component", "--description",
			"--reporter", "--assignee", "--resolution", "--createdDate", "--updatedDate", "--duedate", "--priority",
			"--fixVersion", "--labels");

	private static com.razorthink.utils.jira.api.JiraService js = new com.razorthink.utils.jira.api.JiraService();
	private JiraRestClient restClient;

	@Override
	public Boolean validate(String[] commandTokens) {
		if (!commandTokens[0].equals("jira")) {
			return false;
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
			case "-u":
				if (!iterator.hasNext() || (iterator.hasNext() && jiraCommands.contains(iterator.next()))) {
					return false;
				}
				if (!iterator.hasNext() || (iterator.hasNext() && !iterator.next().equals("-p"))) {
					return false;
				}
				if (!iterator.hasNext() || (iterator.hasNext() && jiraCommands.contains(iterator.next()))) {
					return false;
				}
				if (!iterator.hasNext() || (iterator.hasNext() && !iterator.next().equals("-url"))) {
					return false;
				}
				if (!iterator.hasNext() || (iterator.hasNext() && jiraCommands.contains(iterator.next()))) {
					return false;
				}
				break;
			case "-getProjects":
				if (iterator.hasNext()) {
					return false;
				}
				break;
			case "-getIssues": {
				while (iterator.hasNext()) {
					if (followUpCommands.contains(iterator.next())) {
						String paramValue = iterator.next();
						if (!iterator.hasNext() || (iterator.hasNext()
								&& (jiraCommands.contains(paramValue) || followUpCommands.contains(paramValue)))) {
							return false;
						}
					} else {
						iterator.previous();
						break;
					}
				}

				if (!iterator.hasNext() || (iterator.hasNext() && !iterator.next().equals("--project"))) {
					return false;
				}
				if (!iterator.hasNext() || (iterator.hasNext() && jiraCommands.contains(iterator.next()))) {
					return false;
				}
				break;
			}
			case "-getStatus":
			case "-getIssueType":
			case "-getComponents":
			case "-getDescription":
			case "-getReporter":
			case "-getAssignee":
			case "-getResolution":
			case "-getCreationDate":
			case "-getUpdateDate":
			case "-getDueDate":
			case "-getPriority":
			case "-getVotes":
			case "-getFixVersions":
			case "-getComments":
			case "-getWatchers":
			case "-getLabel":

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
			}
		}
		return true;
	}

	@Override
	public Boolean login(String username, String password, String url) {
		try {
			System.out.println("\n restclient=" + restClient);
			restClient = js.authorize(url, username, password);
			return true;
		} catch (Exception e) {
			throw new DataException("302", "Could not login");
		}
	}

	@Override
	public List<JiraIssue> jqlBuilder(String[] commandTokens) {
		return null;
	}

	@Override
	public List<BasicProject> getAllProjects() {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		List<BasicProject> retrievedProjects;
		try {
			retrievedProjects = js.getProjects();
		} catch (JiraException e) {
			throw new DataException("500", "Internal Server Error");
		}
		return retrievedProjects;
	}

	@Override
	public String getStatus(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		return retrievedIssue.get(0).getStatus().getName();
	}

	@Override
	public String getIssueType(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		return retrievedIssue.get(0).getIssueType().getName();
	}

	@Override
	public String getComponents(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		StringBuilder components= new StringBuilder("");
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		List<BasicComponent> retrievedComponents = (List<BasicComponent>) retrievedIssue.get(0).getComponents();
		if(retrievedComponents.isEmpty()){
			return null;
		}
		int i=0;
		for (BasicComponent component:retrievedComponents ){
			if(i !=0){
				components.append(", ");
			}
			components.append(component.getName());
			i++;	
		}
		return components.toString();
	}

	@Override
	public String getDesription(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		return retrievedIssue.get(0).getDescription();
	}

	@Override
	public String getReporter(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if(retrievedIssue.get(0).getReporter() == null){
			return null;
		}
		return retrievedIssue.get(0).getReporter().getName();
	}

	@Override
	public String getAssignee(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if(retrievedIssue.get(0).getAssignee() == null){
			return null;
		}
		return retrievedIssue.get(0).getAssignee().getName();
	}

	@Override
	public String getResolution(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if(retrievedIssue.get(0).getResolution() == null){
			return null;
		}
		return retrievedIssue.get(0).getResolution().getName();
	}

	@Override
	public String getCreationDate(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		return retrievedIssue.get(0).getCreationDate().toString();
	}

	@Override
	public String getUpdateDate(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if(retrievedIssue.get(0).getUpdateDate() ==null){
			return null;
		}
		return retrievedIssue.get(0).getUpdateDate().toString();
	}

}
