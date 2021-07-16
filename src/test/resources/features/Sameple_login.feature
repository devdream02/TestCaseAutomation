@TBC
Feature: Verify login flow

  Scenario: Verify successfull login
    Given user enter right username and password
    When user click on login button
    Then user be able to succesfully login

  Scenario: Verify failed login
    Given user enter wrong username and password
    When user click on login button
    Then user should not be able to login