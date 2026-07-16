Feature: Test
  Scenario Outline: Missed Example
    Given I have <foo> cucumbers
    And I have <boo> cucumbers
    And I have <doo> cucumbers
    Examples:
      | <caret>foo | boo | doo |
