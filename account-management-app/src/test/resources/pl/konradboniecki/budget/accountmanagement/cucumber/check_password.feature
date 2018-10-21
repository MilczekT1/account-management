Feature: Password check

  Background:
    Given I'm authenticated with Basic Auth
    And account for email johnDoe@mail.com already exist

  Scenario: Password check with valid value succeed
    Given I'm authenticated with Basic Auth
    When I check valid password for account johnDoe@mail.com
    Then the operation is successful

  Scenario: Password check with invalid value fails
    Given I'm authenticated with Basic Auth
    When I check invalid password for account johnDoe@mail.com
    Then the operation is unsuccessful

  Scenario: Password check fails for missing account
    Given I'm authenticated with Basic Auth
    When I check invalid password for account missingJohnDoe@mail.com
    Then account is not found

  Scenario: Password check has to be authorized
    Given I'm not authenticated
    When I check valid password for account johnDoe@mail.com
    Then the operation is unauthorized

