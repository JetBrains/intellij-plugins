Feature: test
  Scenario: test
    Given step
    Given <warning descr="Undefined step reference: test">te<caret>st</warning>
    Given <warning descr="Undefined step reference: super test">super test</warning>
    Given <warning descr="Undefined step reference: test">test</warning>

