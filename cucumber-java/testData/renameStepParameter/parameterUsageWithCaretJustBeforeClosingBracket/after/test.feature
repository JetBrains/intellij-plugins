Feature: Foo

  Scenario Outline: eating
    Given there are <helloDarkness> cucumbers
    Given and are <helloDarkness> cucumbers
    Given and are <helloDarkness> cucumbers
    When I eat <eat> cucumbers
    Then I should have <left> cucumbers

    Examples:
      | helloDarkness | eat | left |
      | 12    | 5   | 7    |
      | 20    | 5   | 15   |
