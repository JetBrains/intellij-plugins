Feature: Foo

  Scenario Outline: eating
    Given there are <newStart> cucumbers
    Given and are <newStart> cucumbers
    When I eat <eat> cucumbers
    Then I should have <left> cucumbers

    Examples:
      | newStart | eat | left |
      | 12    | 5   | 7    |
      | 20    | 5   | 15   |
