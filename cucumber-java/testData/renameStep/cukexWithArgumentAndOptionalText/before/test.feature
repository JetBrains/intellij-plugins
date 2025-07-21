Feature: Rename of cukex with argument and optional text.

  Scenario: First
    Given unrelated step
    And I have 1 feeling
    Then unrelated step
    And I have 2 feelings

  Scenario: Second
    Given I have<caret> 21 feelings
    When unrelated step
    And I have 0 feeling
