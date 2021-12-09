Feature: Foo
  Scenario Outline: inline rename test
    Given <step> like <des<caret>c>
    Examples:
      | step | desc   |
      | 1    | first  |
      | 2    | second |



