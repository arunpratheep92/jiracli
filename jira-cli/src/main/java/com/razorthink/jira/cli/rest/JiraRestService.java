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
			try
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
						if( jiraService.login(username, password, url) && !iterator.hasNext() )
						{
							return "login success";
						}
						break;
					}
					case "-getProjects" :
						return jiraService.getAllProjects().toString();
					case "-getIssues" :
						List<JiraIssue> jqlResult = jiraService.getAllIssues(commandToken);
						return JSONUtils.toJson(jqlResult);
					case "-getStatus" :
						iterator.next();
						String issue = iterator.next();
						iterator.next();
						String project = iterator.next();
						return jiraService.getStatus(issue, project);
					case "-getIssueType" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getIssueType(issue, project);
					case "-getComponents" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getComponents(issue, project);
					case "-getDescription" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getDesription(issue, project);
					case "-getReporter" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getReporter(issue, project);
					case "-getAssignee" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getAssignee(issue, project);
					case "-getResolution" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getResolution(issue, project);
					case "-getCreationDate" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getCreationDate(issue, project);
					case "-getUpdateDate" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getUpdateDate(issue, project);
					case "-getDueDate" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getDueDate(issue, project);
					case "-getPriority" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getPriority(issue, project);
					case "-getVotes" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getVotes(issue, project);
					case "-getFixVersions" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getFixVersions(issue, project);
					case "-getComments" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getComments(issue, project);
					case "-getWatchers" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getWatchers(issue, project);
					case "-getLabel" :
						iterator.next();
						issue = iterator.next();
						iterator.next();
						project = iterator.next();
						return jiraService.getLabels(issue, project);
					case "-jql" :
						StringBuilder jqlValue = new StringBuilder("");
						while( iterator.hasNext() )
						{
							jqlValue.append(iterator.next()).append(" ");
						}
						List<JiraIssue> jqlResult1 = jiraService.getJqlResult(jqlValue.toString());
						return JSONUtils.toJson(jqlResult1);
					case "--help" :
						return jiraService.getHelp();
				}
			}
			catch( Exception e )
			{
				return e.getMessage();
			}
		}
		return null;
	}
}
