package Utility;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

class IMJiraHelper {

    private static JiraRestClient restClient;

    IMJiraHelper(String jiraUserName, String jiraPassword) throws URISyntaxException {
        String jiraUrl = ConfigReader.getProperty("jira.url");
        URI uri = new URI(jiraUrl);
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        try {
            restClient = factory.createWithBasicHttpAuthentication(uri, jiraUserName, jiraPassword);
        }
        catch (NullPointerException e) {
            e.getMessage();
        }
    }

    String createFeature(String projectKey, Long issueType, String issueSummary, String automated) {
        IssueRestClient issueClient = restClient.getIssueClient();
        IssueInputBuilder iib = new IssueInputBuilder();
        iib.setProjectKey(projectKey);
        iib.setSummary(issueSummary);
        iib.setIssueTypeId(issueType);
        iib.setDescription(issueSummary);
        iib.setFieldInput(new FieldInput("<jira_custom_field_id>", ComplexIssueInputFieldValue.with("value",automated)));
        IssueInput issue = iib.build();
        return issueClient.createIssue(issue).claim().getKey();
    }

    String createScenario(String projectKey, Long issueType, String issueSummary, String automated, String desc, String parentKey, String inwardIssue) {
        IssueRestClient issueClient = restClient.getIssueClient();
        IssueInputBuilder iib = new IssueInputBuilder();

        if (!StringUtils.isBlank(inwardIssue)){
            iib.setFieldValue("<jira_custom_field_id>", inwardIssue);
        }
        iib.setProjectKey(projectKey);
        iib.setSummary(issueSummary);
        iib.setIssueTypeId(issueType);
        iib.setDescription(desc);
        Map<String, Object> parent = new HashMap<>();
        parent.put("key", parentKey);
        iib.setFieldInput(new FieldInput("parent", new ComplexIssueInputFieldValue(parent)));
        iib.setFieldInput(new FieldInput("<jira_custom_field_id>", ComplexIssueInputFieldValue.with("value",automated)));
        IssueInput issue = iib.build();
        return issueClient.createIssue(issue).claim().getKey();
    }

    void updateScenario(String issueSummary, String automated, String desc, String key, String inwardIssue) {
        IssueRestClient issueClient = restClient.getIssueClient();
        IssueInputBuilder iib = new IssueInputBuilder();
        if (!StringUtils.isBlank(inwardIssue)){
            iib.setFieldValue("<jira_custom_field_id>", inwardIssue);
        }
        iib.setSummary(issueSummary);
        iib.setDescription(desc);
        iib.setFieldInput(new FieldInput("<jira_custom_field_id>", ComplexIssueInputFieldValue.with("value",automated)));
        IssueInput issue = iib.build();
        issueClient.updateIssue(key,issue).claim();
        System.out.println("Issue updated succesfully");
    }
}
