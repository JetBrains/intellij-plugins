Feature: Parameter Type test

  Scenario: Parameter Type resolve
    When today is 2018-08-31
    When 10 is int
    When 10.0 is float
    When word is word
    When "text with space" is string

    When There is biginteger 10
    When There is bigdecimal 10.0
    When There is long 10
    When There is short 10
    When There is byte 10
    When There is double 10.0
    When I have 10 cucumbers in my belly
    When I have 1 cucumber in my belly
