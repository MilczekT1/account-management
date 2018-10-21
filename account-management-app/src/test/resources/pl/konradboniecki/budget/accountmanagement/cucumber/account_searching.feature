Feature: Account Searching

  Background:
    Given I'm authenticated with Basic Auth
    And account for email johnDoe@mail.com already exist

  Scenario: Searching for existing account by email
    Given I'm authenticated with Basic Auth
    When I get an account by email johnDoe@mail.com
    Then account is found
    And response account does not contain password

  Scenario: Searching for existing account by id
    Given I'm authenticated with Basic Auth
    When I get an account by id for email johnDoe@mail.com
    Then account is found
    And response account does not contain password

  Scenario: Searching for not existing account
    Given I'm authenticated with Basic Auth
    When I get an account by email missingAccount@mail.com
    Then account is not found

  Scenario: Account search has to be authorized
    Given I'm not authenticated
    When I get an account by email johnDoe@mail.com
    Then the operation is unauthorized
