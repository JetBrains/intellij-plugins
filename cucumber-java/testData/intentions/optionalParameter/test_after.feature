Feature: test

  Scenario Outline: test
    Then the "<field>" field should contain "<value>"
    Examples:
      | field       | value |
      | search_text | alpha |