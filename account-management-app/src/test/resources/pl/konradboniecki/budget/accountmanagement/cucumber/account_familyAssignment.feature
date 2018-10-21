Feature: Family assignment to account

  Background:
    Given I'm authenticated with Basic Auth
    And account for email johnDoe@mail.com already exist

  @NeedStaticData
  Scenario: Assignment of family to account
    Given I'm authenticated with Basic Auth
    And family 'BDDTestFamily' exists
    When I assign account with email johnDoe@mail.com to existing family 'BDDTestFamily'
    Then the operation is successful

  Scenario: Assignment of missing family to account
    Given I'm authenticated with Basic Auth
    When I assign account with email johnDoe@mail.com to missing family
    Then family is not found

  Scenario: Assignment of family to missing account
    Given I'm authenticated with Basic Auth
    When I assign missing account to missing family
    Then account is not found

  Scenario: Family assignment has to be authorized
    Given I'm not authenticated
    When I assign account with email johnDoe@mail.com to missing family
    Then the operation is unauthorized
