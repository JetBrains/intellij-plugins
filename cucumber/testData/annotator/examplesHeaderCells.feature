Feature: Test
  Scenario Outline: Missed Example
    Given I have <foo> cucumbers
    And I have <boo> cucumbers
    And I have <doo> cucumbers
  Examples:
    | <info descr="null">foo</info> | <info descr="null">boo</info> | <info descr="null">doo</info> |
    | 1   | 2   | 3   |
