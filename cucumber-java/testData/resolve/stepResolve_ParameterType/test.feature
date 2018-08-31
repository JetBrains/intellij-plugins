Feature: Parameter Type test

  Scenario: Parameter Type resolve
    When today is 2018-08-31
    When 10 is int
    When 10.0 is float
    When word is word
    When "text with space" is string
