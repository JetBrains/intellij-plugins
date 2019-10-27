Feature: test
  Scenario: passing
    Given normal step
    And step with parameter "param"

  Scenario: failing
    Given normal step
    Given failing step

  Scenario: failing comparison
    Given normal step
    Given failing comparing step

  Scenario: pending
    Given normal step
    Given pending step

  Scenario: undefined
    Given normal step
    Given undefined step

  Scenario: lambda passing
    Given normal step lambda

  Scenario: lambda failing
    Given normal step lambda
    Given failing step lambda

  Scenario: lambda pending
    Given normal step lambda
    Given pending step lambda

  Scenario Outline: outline
    Given normal step
    And step with parameter "<param>"
    And step with parameter "<param>"
    Examples:
      | param  |
      | value1 |
      | value2 |