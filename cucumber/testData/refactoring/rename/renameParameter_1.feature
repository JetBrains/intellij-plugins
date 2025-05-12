Feature: Foo

  Our feature desc here

  Scenario Outline: inline rename test
    Given <step> like <de<caret>sc>
    Examples:
      | step | desc   |
      | 1    | first  |
      | 2    | second |



