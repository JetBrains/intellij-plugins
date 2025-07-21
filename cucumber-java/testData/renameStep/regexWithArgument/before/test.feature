Feature: Rename of regex with argument.

  Scenario: First
    Given unrelated step
    And I <caret>withdraw 21 EUR
    Then unrelated step
    And I withdraw 37 EUR

  Scenario: Second
    Given I withdraw 0 EUR
    When unrelated step
    And I withdraw 213742069 EUR
