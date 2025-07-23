Feature: feature
  Scenario Outline: Some title 2
    Given I expect inspection warning on <<type>> with messages 2
    Examples:
      | type   |
      | class  |
      | method |
      | field  |

  Scenario Outline: Some title 3
    Given I expect inspection warning on < <type>> with messages 3
    Examples:
      | type   |
      | class  |
      | method |
      | field  |
