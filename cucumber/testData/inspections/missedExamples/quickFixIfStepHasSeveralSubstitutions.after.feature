Feature: Test
  Scenario Outline: Missed Example
    Given I have <foo> cucumbers <boo> tomates <doo> apples
    Examples:
      | <caret>foo | boo | doo |
