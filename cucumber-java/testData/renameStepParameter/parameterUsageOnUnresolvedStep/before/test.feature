Feature: Foo

  Scenario Outline: eating
    Given there are <sta<caret>rt> cucumbers
    Given and are <start> cucumbers
    Given and are <start> cucumbers
    When I eat <eat> cucumbers
    Then I should have <left> cucumbers

    Examples:
      | start | eat | left |
      | 12    | 5   | 7    |
      | 20    | 5   | 15   |
