package com.razorthink.jira.cli.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.util.concurrent.Promise;
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
		StringBuilder components = new StringBuilder("");
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		List<BasicComponent> retrievedComponents = (List<BasicComponent>) retrievedIssue.get(0).getComponents();
		if (retrievedComponents.isEmpty()) {
			return null;
		}
		int i = 0;
		for (BasicComponent component : retrievedComponents) {
			if (i != 0) {
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
		if (retrievedIssue.get(0).getReporter() == null) {
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
		if (retrievedIssue.get(0).getAssignee() == null) {
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
		if (retrievedIssue.get(0).getResolution() == null) {
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
		if (retrievedIssue.get(0).getUpdateDate() == null) {
			return null;
		}
		return retrievedIssue.get(0).getUpdateDate().toString();
	}

	@Override
	public String getDueDate(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if (retrievedIssue.get(0).getDueDate() == null) {
			return null;
		}
		return retrievedIssue.get(0).getDueDate().toString();
	}

	@Override
	public String getPriority(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if (retrievedIssue.get(0).getPriority() == null) {
			return null;
		}
		return retrievedIssue.get(0).getPriority().getName();
	}

	@Override
	public String getVotes(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if (retrievedIssue.get(0).getVotes() == null) {
			return null;
		}
		return String.valueOf(retrievedIssue.get(0).getVotes().getVotes());
	}

	@Override
	public String getFixVersions(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		StringBuilder versions = new StringBuilder("");
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		List<Version> retrievedFixVersions = (List<Version>) retrievedIssue.get(0).getFixVersions();
		if (retrievedFixVersions.isEmpty()) {
			return null;
		}
		int i = 0;
		for (Version version : retrievedFixVersions) {
			if (i != 0) {
				versions.append(", ");
			}
			versions.append(version.getName());
			i++;
		}
		return versions.toString();
	}

	@Override
	public String getComments(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		StringBuilder comments = new StringBuilder("");
		IssueRestClient issueClient = restClient.getIssueClient();
		Promise<Issue> retrievedIssue = issueClient.getIssue(issue);
		List<Comment> retrievedComments;
		try {
			retrievedComments = (List<Comment>) retrievedIssue.get().getComments();
		} catch (InterruptedException | ExecutionException e) {
			return null;
		}
		if (retrievedComments.isEmpty()) {
			return null;
		}
		int i = 0;
		for (Comment comment : retrievedComments) {
			if (i != 0) {
				comments.append(",\n");
			}
			comments.append(comment.getAuthor().getName()+" : "+comment.getBody());
			i++;
		}
		return comments.toString();
	}

	@Override
	public String getWatchers(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if (retrievedIssue.get(0).getWatchers() == null) {
			return null;
		}
		return String.valueOf(retrievedIssue.get(0).getWatchers().getNumWatchers());
	}

	@Override
	public String getLabels(String issue, String project) {
		if (restClient == null) {
			throw new DataException("403", "Unauthorized");
		}
		StringBuilder labels = new StringBuilder("");
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		Set<String> retrievedLabels = retrievedIssue.get(0).getLabels();
		if (retrievedLabels.isEmpty()) {
			return null;
		}
		int i = 0;
		for (String label : retrievedLabels) {
			if (i != 0) {
				labels.append(", ");
			}
			labels.append(label);
			i++;
		}
		return labels.toString();

	}

	@Override
	public String getJqlResult(String jqlValue) {
		Iterable<Issue> retrievedissue = restClient.getSearchClient().searchJql(jqlValue).claim().getIssues();
		for(Issue issue:retrievedissue){
			
		}
		return null;
	}

}
