Feature: Rename of regex with argument.

  Scenario: First
    Given unrelated step
    And I spend 21 USD
    Then unrelated step
    And I spend 37 USD

  Scenario: Second
    Given I spend 0 USD
    When unrelated step
    And I spend 213742069 USD
