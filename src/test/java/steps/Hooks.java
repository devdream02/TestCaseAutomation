package steps;

import Utility.TestCase;
import io.cucumber.core.api.Scenario;
import io.cucumber.java.After;
import io.cucumber.java.Before;

import java.io.IOException;
import java.net.URISyntaxException;


public class Hooks {
    @Before
    public void login() {
    }

    @After
    public void clean() {
    }

    @After("@TBC")
    public void jira(Scenario scenario) throws IOException, URISyntaxException {
        TestCase testCase = new TestCase();
        testCase.testCaseCreation(scenario);
    }

    @After("@TBU")
    public void jiraUpdate(Scenario scenario) throws URISyntaxException, IOException {
        TestCase testCase = new TestCase();
        testCase.testCaseUpdation(scenario);
    }
}
