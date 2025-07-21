Feature: Rename of cukex with argument and optional text.

  Scenario: First
    Given unrelated step
    And I really do have 1 feeling
    Then unrelated step
    And I really do have 2 feelings

  Scenario: Second
    Given I really do have<caret> 21 feelings
    When unrelated step
    And I really do have 0 feeling
