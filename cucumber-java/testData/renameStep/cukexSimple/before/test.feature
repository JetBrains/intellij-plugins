Feature: Rename of simple cukex.

  Scenario: First
    Given I am <caret>happy
    And I am dumb
    Then I am happy

  Scenario: Second
    Given I am happy
    Then unrelated step