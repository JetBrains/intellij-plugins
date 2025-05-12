Feature: Foo

  Our feature desc here

  Scenario Outline: inline rename test
    Given <step> like <newDescription>
    Examples:
      | step | newDescription |
      | 1    | first          |
      | 2    | second         |



