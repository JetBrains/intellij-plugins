Feature: Some interesting feature

  Scenario Outline: eating
    Given there are <start> and <start> cucumbers
    Given there are <start> cucumbers
    Given there are <start> cucumbers
    When I eat <eat> cucumbers
    Then I should have <left> cucumbers

    Examples:
      | sta<caret>rt | eat | left |
      | 12    | 5   | 7    |
      | 20    | 5   | 15   |
