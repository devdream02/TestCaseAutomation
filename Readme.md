**Test Case Automation**

Use case:- Automatically creating test cases in JIRA from the feature file scenarios and to update them automatically once they are executed using Serenity Jira workflow update feature

Mapping of BDD features with Manual test case

- Test Feature symbolizes Feature/Functionality
- Test Sceanrio symbolizes Test case under the functionality

**Framework used:-** 
Sernity BDD with cucumber


**Prerequistes in Jira:-**
1. Jira setup, valid URL, credentials
2. Create 2 new issue type in JIRA:- Test case(Sub task) ~ Scenario, Test Feature(Main Task) ~ Feature
3. One custom field in JIRA to identify whether the scenario is automated or not. (Remember to update the custom field value in IMJiraHelper class under place holder <jira_custom_field_id> )

Implement you cucumber BDD framework, we will leverage the hooks feature of cucumber to trigger out test case creation function and jira Rest APis to create the test cases in JIRA


**First use case**:- When you want you BDD feature files and Scenarios to be created in JIRA for the first time
Tag your feature file with @TBC tag, it will create corresponding JIRA ticket Test Feature --> Test Case, also it will update your Test case feature file with @<JIRA-ID> for furture refrence 

**Second use case**:- Lets say you have changed your existing test case and want to update the same in JIRA 
Tag your feature file with @TBU tag, it will update the corresponding JIRA test case whose ID is already there @<JIRA-ID> tag created previously.

**Manual Test Case**
For manual test cases give additional tag @manual on the Feature level it will update created the JIRA test case with Automated: False custom field which you created.

Consider the sample feature file in resources folder

**Note:-** 
1. All the above tags can be given both at feature level or at scenario level, if given at feature level then no need of giving them at scenario level individually. 
2. Update issueType id in testcase.java file with the unique id you would have created in JIRA.



