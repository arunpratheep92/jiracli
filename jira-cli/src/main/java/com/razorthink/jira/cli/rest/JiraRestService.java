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
import com.razorthink.jira.cli.domain.JiraIssue;
import com.razorthink.jira.cli.service.JiraService;
import com.razorthink.utils.cmutils.JSONUtils;

@RestController
@RequestMapping( "/jiraServices" )
@Component
public class JiraRestService {

	@Autowired
	JiraService jiraService;

	@Consumes( { MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON } )
	@Produces( { MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON } )
	@RequestMapping( value = "/commands", method = RequestMethod.POST )
	public String jiraService( @RequestBody String commands )
	{
		commands = commands.replaceAll("\\s+", " ");
		commands = commands.trim();
		Pattern regex = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
		Matcher regexMatcher = regex.matcher(commands);
		List<String> commandToken = new ArrayList<>();
		while( regexMatcher.find() )
		{
			if( regexMatcher.group(1) != null )
			{
				commandToken.add(regexMatcher.group(1));
			}
			else if( regexMatcher.group(2) != null )
			{
				commandToken.add(regexMatcher.group(2));
			}
			else
			{
				commandToken.add(regexMatcher.group());
			}
		}
		if( !jiraService.validate(commandToken) )
		{
			return "Invalid Syntax";
		}
		ListIterator<String> iterator = commandToken.listIterator();
		while( iterator.hasNext() )
		{
			switch ( iterator.next() )
			{
				case "jira" :
					break;
				case "-u" :{
					String username = iterator.next();
					iterator.next();
					String password = iterator.next();
					iterator.next();
					String url = iterator.next();
					try
					{
						if( jiraService.login(username, password, url) && !iterator.hasNext() )
						{
							return "login success";
						}
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
					break;
				}
				case "-getProjects" :{
					try
					{
						return jiraService.getAllProjects().toString();
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				}
				case "-getIssues" :{
					try
					{
						List<JiraIssue> jqlResult = jiraService.getAllIssues(commandToken);
						return JSONUtils.toJson(jqlResult);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				}
				case "-getStatus" :
					iterator.next();
					String issue = iterator.next();
					iterator.next();
					String project = iterator.next();
					try
					{
						return jiraService.getStatus(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getIssueType" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getIssueType(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getComponents" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getComponents(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getDescription" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getDesription(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getReporter" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getReporter(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getAssignee" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getAssignee(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getResolution" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getResolution(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getCreationDate" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getCreationDate(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getUpdateDate" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getUpdateDate(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getDueDate" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getDueDate(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getPriority" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getPriority(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getVotes" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getVotes(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getFixVersions" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getFixVersions(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getComments" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getComments(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getWatchers" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getWatchers(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "-getLabel" :
					iterator.next();
					issue = iterator.next();
					iterator.next();
					project = iterator.next();
					try
					{
						return jiraService.getLabels(issue, project);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}

				case "-jql" :
					StringBuilder jqlValue = new StringBuilder("");
					while( iterator.hasNext() )
					{
						jqlValue.append(iterator.next()).append(" ");
					}
					try
					{
						List<JiraIssue> jqlResult = jiraService.getJqlResult(jqlValue.toString());
						return JSONUtils.toJson(jqlResult);
					}
					catch( Exception e )
					{
						return e.getMessage();
					}
				case "--help" :
					return jiraService.getHelp();
			}
		}
		return null;
	}
}
