package com.razorthink.jira.cli.service.impl;

import java.util.ArrayList;
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
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.Subtask;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.util.concurrent.Promise;
import com.razorthink.jira.cli.domain.JiraIssue;
import com.razorthink.jira.cli.domain.JiraSubtask;
import com.razorthink.jira.cli.exception.DataException;
import com.razorthink.jira.cli.service.JiraService;
import com.razorthink.utils.cmutils.NullEmptyUtils;
import net.rcarz.jiraclient.JiraException;

@Service
public class JiraServiceImpl implements JiraService {

	static List<String> jiraCommands = Arrays.asList("-u", "-p", "-url", "-getProjects", "-getIssues", "-getStatus",
			"-getIssueType", "-getComponents", "-getDescription", "-getReporter", "-getAssignee", "-getResolution",
			"-getCreationDate", "-getUpdateDate", "-getDueDate", "-getPriority", "-getVotes", "-getFixVersions",
			"-getComments", "-getWatchers", "-getLabels");

	static List<String> followUpCommands = Arrays.asList("--status", "--issuetype", "--component", "--description",
			"--reporter", "--assignee", "--resolution", "--createdDate", "--updatedDate", "--duedate", "--priority",
			"--fixVersion", "--sprint", "--labels,--help");
	static StringBuilder str = new StringBuilder("");
	static String help = str.append("-u\t\t:   Specify the user name").append("\n")
			.append("-p\t\t:   Specify the password").append("\n").append("-url\t\t:   Specify the jira url")
			.append("\n")
			.append("-getProjects\t:    Retrieves all the projects in the atlassian account specified by the url")
			.append("\n").append("-getIssues\t:    Retrieves all the issues for the specified project").append("\n")
			.append("-getStatus\t:    Returns the current status of the specified issue").append("\n")
			.append("-getIssueType\t:    Returns the type of specified issue").append("\n")
			.append("-getComponents\t:    Returns the components of the specified issue").append("\n")
			.append("-getDescription\t:    Returns the description of the specified issue").append("\n")
			.append("-getReporter\t:    Returns the reporter of the specified issue").append("\n")
			.append("-getAssignee\t:    Returns the assignee of the specified issue").append("\n")
			.append("-getResolution\t:    Returns the resolution of the specified issue").append("\n")
			.append("-getCreationDate:    Returns the creation date of the specified issue").append("\n")
			.append("-getUpdateDate\t:    Returns the updated date of the specified issue").append("\n")
			.append("-getDueDate\t:    Returns the due date of the specified issue").append("\n")
			.append("-getPriority\t:    Returns the priority of the specified issue").append("\n")
			.append("-getVotes\t:    Retrieves all the votes for the specified issue").append("\n")
			.append("-getFixVersions\t:    Retrieves all the FixVersions for the specified issue").append("\n")
			.append("-getComments\t:    Retrieves all the comments for the specified issue").append("\n")
			.append("-getWatchers\t:    Returns the list of watchers for the specified issue").append("\n")
			.append("-getLabel\t:    Returns the label for the specified issue").append("\n")
			.append("-jql\t\t:    Performs an advanced search in JIRA using Jira Query Language").toString();
	private static com.razorthink.utils.jira.api.JiraService js = new com.razorthink.utils.jira.api.JiraService();
	private JiraRestClient restClient;

	@Override
	public Boolean validate( List<String> commandToken )
	{
		if( !commandToken.get(0).equals("jira") )
		{
			return false;
		}
		ListIterator<String> iterator = commandToken.listIterator();
		while( iterator.hasNext() )
		{
			switch ( iterator.next() )
			{
				case "jira" :
					break;
				case "--help" :
					if( iterator.hasNext() )
					{
						return false;
					}
					break;
				case "-u" :
					if( !iterator.hasNext() || (iterator.hasNext() && jiraCommands.contains(iterator.next())) )
					{
						return false;
					}
					if( !iterator.hasNext() || (iterator.hasNext() && !iterator.next().equals("-p")) )
					{
						return false;
					}
					if( !iterator.hasNext() || (iterator.hasNext() && jiraCommands.contains(iterator.next())) )
					{
						return false;
					}
					if( !iterator.hasNext() || (iterator.hasNext() && !iterator.next().equals("-url")) )
					{
						return false;
					}
					if( !iterator.hasNext() || (iterator.hasNext() && jiraCommands.contains(iterator.next())) )
					{
						return false;
					}
					break;
				case "-getProjects" :
					if( iterator.hasNext() )
					{
						return false;
					}
					break;
				case "-getIssues" :
					while( iterator.hasNext() )
					{
						if( followUpCommands.contains(iterator.next()) )
						{
							String paramValue = iterator.next();
							if( !iterator.hasNext() || (iterator.hasNext()
									&& (jiraCommands.contains(paramValue) || followUpCommands.contains(paramValue))) )
							{
								return false;
							}
						}
						else
						{
							iterator.previous();
							break;
						}
					}
					if( iterator.hasNext() && iterator.next().equals("--issue") )
					{
						if( !iterator.hasNext() || (iterator.hasNext() && jiraCommands.contains(iterator.next())) )
						{
							return false;
						}
					}
					else
					{
						iterator.previous();
					}
					if( !iterator.hasNext() || (iterator.hasNext() && !iterator.next().equals("--project")) )
					{
						return false;
					}
					if( !iterator.hasNext() || (iterator.hasNext() && jiraCommands.contains(iterator.next())) )
					{
						return false;
					}
					break;
				case "-getStatus" :
				case "-getIssueType" :
				case "-getComponents" :
				case "-getDescription" :
				case "-getReporter" :
				case "-getAssignee" :
				case "-getResolution" :
				case "-getCreationDate" :
				case "-getUpdateDate" :
				case "-getDueDate" :
				case "-getPriority" :
				case "-getVotes" :
				case "-getFixVersions" :
				case "-getComments" :
				case "-getWatchers" :
				case "-getLabel" :
					if( !iterator.hasNext() || (iterator.hasNext() && !iterator.next().equals("--issue")) )
					{
						return false;
					}
					if( !iterator.hasNext() || (iterator.hasNext() && jiraCommands.contains(iterator.next())) )
					{
						return false;
					}
					if( !iterator.hasNext() || (iterator.hasNext() && !iterator.next().equals("--project")) )
					{
						return false;
					}
					if( !iterator.hasNext() || (iterator.hasNext() && jiraCommands.contains(iterator.next())) )
					{
						return false;
					}
					break;
				case "-jql" :
					while( iterator.hasNext() )
					{
						String jqlValue = iterator.next();
						if( jiraCommands.contains(jqlValue) || followUpCommands.contains(jqlValue) )
						{
							return false;
						}
					}
					break;
				default :
					return false;
			}
		}
		return true;
	}

	@Override
	public Boolean login( String username, String password, String url )
	{
		try
		{
			restClient = js.authorize(url, username, password);
			restClient.getProjectClient().getAllProjects().claim();
			return true;
		}
		catch( Exception e )
		{
			throw new DataException("401", "Could not login");
		}
	}

	@Override
	public List<BasicProject> getAllProjects()
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		List<BasicProject> retrievedProjects;
		try
		{
			retrievedProjects = js.getProjects();
		}
		catch( JiraException e )
		{
			throw new DataException("500", "Internal Server Error");
		}
		return retrievedProjects;
	}

	@Override
	public String getStatus( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		return retrievedIssue.get(0).getStatus().getName();
	}

	@Override
	public String getIssueType( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		return retrievedIssue.get(0).getIssueType().getName();
	}

	@Override
	public String getComponents( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		StringBuilder components = new StringBuilder("");
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		List<BasicComponent> retrievedComponents = (List<BasicComponent>) retrievedIssue.get(0).getComponents();
		if( retrievedComponents.isEmpty() )
		{
			return null;
		}
		int i = 0;
		for( BasicComponent component : retrievedComponents )
		{
			if( i != 0 )
			{
				components.append(", ");
			}
			components.append(component.getName());
			i++;
		}
		return components.toString();
	}

	@Override
	public String getDesription( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		return retrievedIssue.get(0).getDescription();
	}

	@Override
	public String getReporter( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if( retrievedIssue.get(0).getReporter() == null )
		{
			return null;
		}
		return retrievedIssue.get(0).getReporter().getName();
	}

	@Override
	public String getAssignee( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if( retrievedIssue.get(0).getAssignee() == null )
		{
			return null;
		}
		return retrievedIssue.get(0).getAssignee().getName();
	}

	@Override
	public String getResolution( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if( retrievedIssue.get(0).getResolution() == null )
		{
			return "Unresolved";
		}
		return retrievedIssue.get(0).getResolution().getName();
	}

	@Override
	public String getCreationDate( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		return retrievedIssue.get(0).getCreationDate().toString();
	}

	@Override
	public String getUpdateDate( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if( retrievedIssue.get(0).getUpdateDate() == null )
		{
			return null;
		}
		return retrievedIssue.get(0).getUpdateDate().toString();
	}

	@Override
	public String getDueDate( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if( retrievedIssue.get(0).getDueDate() == null )
		{
			return null;
		}
		return retrievedIssue.get(0).getDueDate().toString();
	}

	@Override
	public String getPriority( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if( retrievedIssue.get(0).getPriority() == null )
		{
			return null;
		}
		return retrievedIssue.get(0).getPriority().getName();
	}

	@Override
	public String getVotes( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if( retrievedIssue.get(0).getVotes() == null )
		{
			return null;
		}
		return String.valueOf(retrievedIssue.get(0).getVotes().getVotes());
	}

	@Override
	public String getFixVersions( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		StringBuilder versions = new StringBuilder("");
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		List<Version> retrievedFixVersions = (List<Version>) retrievedIssue.get(0).getFixVersions();
		if( retrievedFixVersions.isEmpty() )
		{
			return null;
		}
		int i = 0;
		for( Version version : retrievedFixVersions )
		{
			if( i != 0 )
			{
				versions.append(", ");
			}
			versions.append(version.getName());
			i++;
		}
		return versions.toString();
	}

	@Override
	public String getComments( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		StringBuilder comments = new StringBuilder("");
		IssueRestClient issueClient = restClient.getIssueClient();
		Promise<Issue> retrievedIssue = issueClient.getIssue(issue);
		List<Comment> retrievedComments;
		try
		{
			retrievedComments = (List<Comment>) retrievedIssue.get().getComments();
		}
		catch( InterruptedException | ExecutionException e )
		{
			return null;
		}
		if( retrievedComments.isEmpty() )
		{
			return null;
		}
		int i = 0;
		for( Comment comment : retrievedComments )
		{
			if( i != 0 )
			{
				comments.append(",\n");
			}
			comments.append(comment.getAuthor().getName() + " : " + comment.getBody());
			i++;
		}
		return comments.toString();
	}

	@Override
	public String getWatchers( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		if( retrievedIssue.get(0).getWatchers() == null )
		{
			return null;
		}
		return String.valueOf(retrievedIssue.get(0).getWatchers().getNumWatchers());
	}

	@Override
	public String getLabels( String issue, String project )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		StringBuilder labels = new StringBuilder("");
		List<Issue> retrievedIssue = (List<Issue>) restClient.getSearchClient()
				.searchJql("project=" + project + " and issue = " + issue).claim().getIssues();
		Set<String> retrievedLabels = retrievedIssue.get(0).getLabels();
		if( retrievedLabels.isEmpty() )
		{
			return null;
		}
		int i = 0;
		for( String label : retrievedLabels )
		{
			if( i != 0 )
			{
				labels.append(", ");
			}
			labels.append(label);
			i++;
		}
		return labels.toString();
	}

	@Override
	public List<JiraIssue> getJqlResult( String jqlValue )
	{
		if( restClient == null )
		{
			throw new DataException("403", "Unauthorized");
		}
		List<JiraIssue> issues = new ArrayList<>();
		Iterable<Issue> retrievedissue = restClient.getSearchClient().searchJql(jqlValue).claim().getIssues();
		for( Issue issueValue : retrievedissue )
		{
			Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
			JiraIssue jiraIssue = new JiraIssue();
			try
			{
				jiraIssue.setKey(issue.get().getKey());
				jiraIssue.setStatus(issue.get().getStatus().getName());
				jiraIssue.setIssueType(issue.get().getIssueType().getName());
				jiraIssue.setProject(issue.get().getProject().getName());
				jiraIssue.setSummary(issue.get().getSummary());
				jiraIssue.setDescription(issue.get().getDescription());
				jiraIssue.setReporter(issue.get().getReporter().getName());
				if( issue.get().getAssignee() != null )
				{
					jiraIssue.setAssignee(issue.get().getAssignee().getName());
				}
				else
				{
					jiraIssue.setAssignee("Unassigned");
				}
				if( issue.get().getResolution() != null )
				{
					jiraIssue.setResolution(issue.get().getResolution().getName());
				}
				else
				{
					jiraIssue.setResolution("Unresolved");
				}
				jiraIssue.setCreationDate(issue.get().getCreationDate().toString());
				if( issue.get().getUpdateDate() != null )
				{
					jiraIssue.setUpdateDate(issue.get().getUpdateDate().toString());
				}
				else
				{
					jiraIssue.setUpdateDate("null");
				}
				if( issue.get().getDueDate() != null )
				{
					jiraIssue.setUpdateDate(issue.get().getDueDate().toString());
				}
				else
				{
					jiraIssue.setDueDate("null");
				}
				if( issue.get().getPriority() != null )
				{
					jiraIssue.setPriority(issue.get().getPriority().getName());
				}
				else
				{
					jiraIssue.setPriority("null");
				}
				if( issue.get().getComponents() != null )
				{
					List<String> jiraComponents = new ArrayList<>();
					for( BasicComponent component : issue.get().getComponents() )
					{
						jiraComponents.add(component.getName());
					}
					jiraIssue.setComponents(jiraComponents);

				}
				else
				{
					jiraIssue.setComponents(null);
				}
				if( issue.get().getComments() != null )
				{
					List<String> jiraComments = new ArrayList<>();
					for( Comment comment : issue.get().getComments() )
					{
						jiraComments.add(comment.getAuthor().getName() + " : " + comment.getBody());
					}
					jiraIssue.setComments(jiraComments);

				}
				else
				{
					jiraIssue.setComments(null);
				}
				if( issue.get().getFixVersions() != null )
				{
					List<String> jiraFixVersions = new ArrayList<>();
					for( Version version : issue.get().getFixVersions() )
					{
						jiraFixVersions.add(version.getName());
					}
					jiraIssue.setFixVersions(jiraFixVersions);

				}
				else
				{
					jiraIssue.setFixVersions(null);
				}
				if( issue.get().getAffectedVersions() != null )
				{
					List<String> jiraAffectedVersions = new ArrayList<>();
					for( Version version : issue.get().getAffectedVersions() )
					{
						jiraAffectedVersions.add(version.getName());
					}
					jiraIssue.setAffectedVersions(jiraAffectedVersions);

				}
				else
				{
					jiraIssue.setFixVersions(null);
				}
				if( issue.get().getIssueLinks() != null )
				{
					List<String> jiraIssueLinks = new ArrayList<>();
					for( IssueLink issueLink : issue.get().getIssueLinks() )
					{
						jiraIssueLinks.add(issueLink.getIssueLinkType().getName());
					}
					jiraIssue.setIssueLinks(jiraIssueLinks);

				}
				else
				{
					jiraIssue.setIssueLinks(null);
				}
				if( issue.get().getLabels() != null )
				{
					jiraIssue.setLabels(issue.get().getLabels());

				}
				else
				{
					jiraIssue.setLabels(null);
				}
				if( !NullEmptyUtils.isNullorEmpty((List<?>) issue.get().getFields()) )
				{
					if( issue.get().getFieldByName("Epic Link") != null
							&& issue.get().getFieldByName("Epic Link").getValue() != null )
					{
						jiraIssue.setEpicLink(issue.get().getFieldByName("Epic Link").getValue().toString());
					}
					else
					{
						jiraIssue.setEpicLink("null");
					}
					if( issue.get().getFieldByName("Sprint") != null
							&& issue.get().getFieldByName("Sprint").getValue() != null )
					{
						jiraIssue.setSprint(issue.get().getFieldByName("Sprint").getValue().toString());
					}
					else
					{
						jiraIssue.setSprint("null");
					}
				}
				jiraIssue.setTimeTracking(issue.get().getTimeTracking());
				if( issue.get().getSubtasks() != null )
				{
					List<JiraSubtask> subtask = new ArrayList<>();
					for( Subtask subtasks : issue.get().getSubtasks() )
					{
						JiraSubtask task = new JiraSubtask();
						task.setIssueKey(subtasks.getIssueKey());
						task.setIssueType(subtasks.getIssueType().getName());
						task.setStatus(subtasks.getStatus().getName());
						task.setSummary(subtasks.getSummary());
						subtask.add(task);
					}
					jiraIssue.setSubtasks(subtask);

				}
				else
				{
					jiraIssue.setIssueLinks(null);
				}
				issues.add(jiraIssue);
			}
			catch( Exception e )
			{
				return null;
			}
		}
		return issues;
	}

	@Override
	public List<JiraIssue> getAllIssues( List<String> commandToken )
	{
		StringBuilder jqlValue = new StringBuilder("");
		ListIterator<String> iterator = commandToken.listIterator();
		while( iterator.hasNext() )
		{
			switch ( iterator.next() )
			{
				case "--status" :
					jqlValue.append(" status = ").append(iterator.next()).append(" AND ");
					break;
				case "--issuetype" :
					jqlValue.append(" issuetype = ").append(iterator.next()).append(" AND ");
					break;
				case "--component" :
					jqlValue.append(" component = ").append(iterator.next()).append(" AND ");
					break;
				case "--description" :
					jqlValue.append(" description ~ ").append(iterator.next()).append(" AND ");
					break;
				case "--reporter" :
					jqlValue.append(" reporter = ").append(iterator.next()).append(" AND ");
					break;
				case "--assignee" :
					jqlValue.append(" assignee = ").append(iterator.next()).append(" AND ");
					break;
				case "--resolution" :
					jqlValue.append(" resolution = ").append(iterator.next()).append(" AND ");
					break;
				case "--createdDate" :
					jqlValue.append(" createdDate = ").append(iterator.next()).append(" AND ");
					break;
				case "--updatedDate" :
					jqlValue.append(" updatedDate = ").append(iterator.next()).append(" AND ");
					break;
				case "--duedate" :
					jqlValue.append(" duedate = ").append(iterator.next()).append(" AND ");
					break;
				case "--priority" :
					jqlValue.append(" priority = ").append(iterator.next()).append(" AND ");
					break;
				case "--fixVersion" :
					jqlValue.append(" fixVersion = ").append(iterator.next()).append(" AND ");
					break;
				case "--sprint" :
					jqlValue.append(" Sprint = ").append(iterator.next()).append(" AND ");
					break;
				case "--labels" :
					jqlValue.append(" labels = ").append(iterator.next()).append(" AND ");
					break;
				case "--issue" :
					jqlValue.append(" issue = ").append(iterator.next()).append(" AND ");
					break;
				case "--project" :
					jqlValue.append(" project = ").append(iterator.next());
					break;
			}
		}
		List<JiraIssue> issues = getJqlResult(jqlValue.toString());
		return issues;
	}

	@Override
	public String getHelp()
	{
		return help;
	}

}
