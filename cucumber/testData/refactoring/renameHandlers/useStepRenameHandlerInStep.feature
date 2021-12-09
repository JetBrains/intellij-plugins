Feature: Foo
  Scenario Outline: inline rename test
    Given <step> li<caret>ke <desc>
    Examples:
      | step | desc   |
      | 1    | first  |
      | 2    | second |



