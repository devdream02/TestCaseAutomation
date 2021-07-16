package Utility;

import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Tag;
import io.cucumber.core.api.Scenario;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestCase {

    private IMJiraHelper jira;

    private String inwardIssue = "";

    public TestCase() throws URISyntaxException {
        String jiraUsername = ConfigReader.getProperty("jira.username");
        String jiraPassword = ConfigReader.getProperty("jira.password");
        jira = new IMJiraHelper(jiraUsername, jiraPassword);
    }

    public void testCaseCreation(Scenario scenario) throws IOException {
        String jiraProject = ConfigReader.getProperty("jira.url");
        String url = scenario.getUri();
        url = url.substring(5);
        String gherkinDoc;

        try {
//            gherkinDoc = FixJava.readReader(new InputStreamReader(new FileInputStream(url), StandardCharsets.UTF_8));
            gherkinDoc = new String(Files.readAllBytes(Paths.get(url)));
            Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
            TokenMatcher matcher = new TokenMatcher();
            GherkinDocument gherkinDocument = parser.parse(gherkinDoc, matcher);

            int flag = 0;
            String jira_id=null;
            if(gherkinDocument.getFeature().getTags().stream().noneMatch(s -> s.getName().contains("@JIRA"))){
                //create JIRA issue for feature

                if(gherkinDocument.getFeature().getTags().stream().map(Tag::getName).collect(Collectors.toList()).contains("@manual")){
                    jira_id= jira.createFeature(jiraProject,10079L, gherkinDocument.getFeature().getName(), "No");
                }else
                    jira_id= jira.createFeature(jiraProject,10079L, gherkinDocument.getFeature().getName(), "Yes");

                System.out.println(jira_id);

                //Update feature file with the jira tag
                String content = "@JIRA:"+jira_id;
                Path path = Paths.get(url);
                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                int position = 0;
                int i = 0;
                for(String line: lines){
                    position = line.indexOf("Feature");
                    if(position!=-1) {
                        break;
                    }
                    i++;
                }
                if(position!=-1) {
                    lines.set(i-1, lines.get(i-1)+ " "+content);
                }
                Files.write(path, lines, StandardCharsets.UTF_8);
                flag = 1;
            }

            for (ScenarioDefinition child : gherkinDocument.getFeature().getChildren()) {
                if (child.getName().contains(scenario.getName())){
                    if((child instanceof ScenarioOutline && ((ScenarioOutline) child).getTags().stream().noneMatch(tag -> tag.getName().contains("@issue"))) || (!(child instanceof ScenarioOutline) && scenario.getSourceTagNames().stream().noneMatch(s -> s.contains("@issue")))){
                        List<String> steps = child.getSteps().stream().map(step -> step.getKeyword() + step.getText()).collect(Collectors.toList());
                        if(child instanceof ScenarioOutline){
                            steps.add("Examples");
                            ((ScenarioOutline) child).getExamples()
                                    .forEach(examples -> steps.add(examples.getTableHeader()
                                            .getCells()
                                            .stream()
                                            .flatMap(tableCell -> Stream.of(tableCell.getValue()))
                                            .collect(Collectors.joining("|", "|","|"))));

                            ((ScenarioOutline) child).getExamples()
                                    .forEach(examples -> examples.getTableBody()
                                            .forEach(tableRow -> steps.add(tableRow
                                                    .getCells()
                                                    .stream()
                                                    .flatMap(tableCell -> Stream.of(tableCell.getValue()))
                                                    .collect(Collectors.joining("|", "|","|")))));
                        }

                        String desc = StringUtils.join(steps, '\n');

                        //create JIRA issue for sceanrio
                        String parentKey;
                        if(flag==1){
                            parentKey = jira_id;
                        }else {
                            parentKey = gherkinDocument.getFeature().getTags().stream().filter(s -> s.getName().contains("@JIRA")).map(Tag::getName).collect(Collectors.toList()).get(0).substring(6);
                        }

                        String automated;
                        if(scenario.getSourceTagNames().contains("@manual")){
                            automated = "No";
                        }else
                            automated = "Yes";

                        List<String> tags = (List<String>) scenario.getSourceTagNames();
                        for (String tag : tags) {
                            if (tag.contains("@US")) {
                                inwardIssue = tag.replace("@US", "");
                            }
                        }

                        String issue_id= jira.createScenario(jiraProject,10080L, child.getName(),automated, desc,parentKey, inwardIssue);
                        System.out.println(issue_id);
                        String issueKey= "@issue:"+issue_id;
                        Path path = Paths.get(url);
                        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                        for (String line: lines) {
                            System.out.println(line);
                        }
                        int position = 0;
                        int i = 0;
                        for(String line: lines){
                            position = line.indexOf(scenario.getName());
                            if(position!=-1) {
                                break;
                            }
                            i++;
                        }
                        if(position!=-1) {
                            lines.set(i-1, lines.get(i-1)+ " "+issueKey);
                        }
                        Files.write(path, lines, StandardCharsets.UTF_8);
                    }
                }
            }

        } catch (FileNotFoundException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void testCaseUpdation(Scenario scenario) throws IOException {
        String url = scenario.getUri();
        url = url.substring(5);
        String gherkin;
        try {
//            gherkin = FixJava.readReader(new InputStreamReader(new FileInputStream(url), StandardCharsets.UTF_8));
            gherkin = new String(Files.readAllBytes(Paths.get(url)));
            Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
            TokenMatcher matcher = new TokenMatcher();
            GherkinDocument gherkinDocument = parser.parse(gherkin, matcher);

            if(gherkinDocument.getFeature().getTags().stream().noneMatch(s -> s.getName().contains("@JIRA"))){
                System.out.println("Feature is not present in JIRA board");
            } else {
                String key = gherkinDocument.getFeature().getTags().stream().map(Tag::getName).filter(s -> s.contains("@JIRA")).collect(Collectors.toList()).get(0).substring(6);

                List<String> tags = (List<String>) scenario.getSourceTagNames();

                if(gherkinDocument.getFeature().getTags().stream().map(Tag::getName).collect(Collectors.toList()).contains("@manual")){
                    jira.updateScenario(gherkinDocument.getFeature().getName(), "No", gherkinDocument.getFeature().getName(), key, inwardIssue);
                } else
                    jira.updateScenario(gherkinDocument.getFeature().getName(), "Yes", gherkinDocument.getFeature().getName(), key, inwardIssue);
            }

            for (ScenarioDefinition child : gherkinDocument.getFeature().getChildren()) {
                if (child.getName().contains(scenario.getName())){
                    if(scenario.getSourceTagNames().stream().noneMatch(s -> s.contains("@issue"))) {
                        System.out.println("Issue is not yet created in jira board.");
                    }else{
                        System.out.println("Updating issue.....");
                        List<String> steps = child.getSteps().stream().map(step -> step.getKeyword() + step.getText()).collect(Collectors.toList());

                        if(child instanceof ScenarioOutline){
                            steps.add("Examples");
                            ((ScenarioOutline) child).getExamples()
                                    .forEach(examples -> steps.add(examples.getTableHeader()
                                            .getCells()
                                            .stream()
                                            .flatMap(tableCell -> Stream.of(tableCell.getValue()))
                                            .collect(Collectors.joining("|", "|","|"))));

                            ((ScenarioOutline) child).getExamples()
                                    .forEach(examples -> examples.getTableBody()
                                            .forEach(tableRow -> steps.add(tableRow
                                                    .getCells()
                                                    .stream()
                                                    .flatMap(tableCell -> Stream.of(tableCell.getValue()))
                                                    .collect(Collectors.joining("|", "|","|")))));
                        }

                        String desc = StringUtils.join(steps, '\n');

                        //Update JIRA issue for feature
                        String key = scenario.getSourceTagNames().stream().filter(s -> s.contains("@issue")).collect(Collectors.toList()).get(0).substring(7);

                        List<String> tags = (List<String>) scenario.getSourceTagNames();
                        for (String tag : tags) {
                            if (tag.contains("@US")) {
                                inwardIssue = tag.replace("@US", "");
                            }
                        }

                        String automated;
                        if(scenario.getSourceTagNames().contains("@manual")){
                            automated = "No";
                        }else
                            automated = "Yes";

                        jira.updateScenario(scenario.getName(), automated, desc, key, inwardIssue);
                    }
                }
            }
        } catch (FileNotFoundException | RuntimeException e) {
            e.printStackTrace();
        }
    }
}
