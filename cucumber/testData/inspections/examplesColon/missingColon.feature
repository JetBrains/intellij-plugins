Feature: Test
  Scenario Outline: Missed Example
    Given I open a RSpec test with text
  <error descr="Missing ':' after examples keyword">Examples</error>
    | what |
