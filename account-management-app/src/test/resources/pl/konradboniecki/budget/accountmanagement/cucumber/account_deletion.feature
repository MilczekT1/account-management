@B2B
Feature: Account Deletion

  Scenario: Delete existing account
    Given I'm authenticated with Basic Auth
    And account for email johnDoe@mail.com already exist
    When I delete an account by id for email johnDoe@mail.com
    Then the operation is successful

  Scenario: Delete missing account
    Given I'm authenticated with Basic Auth
    And I delete random account
    Then account is not found

  Scenario: Account deletion has to be authorized
    Given I'm not authenticated
    When I delete random account
    Then the operation is unauthorized
