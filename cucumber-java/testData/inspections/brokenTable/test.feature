Feature: test
  Scenario Outline: test
    Given test
      | param1 | param2 |
      | 1      | 2      |
    Given test
      | param1 | param2 |
  <error descr="Row must have the same cells count as table's header">| 1      |</error>
    Given test
      | param1 | param2 |
  <error descr="Row must have the same cells count as table's header">| 1      | 2      | 3 |</error>
  Examples:
    | <info descr="null">param1</info> | <info descr="null">param2</info> |
    | 1      | 2      |
  Examples:
    | <info descr="null">param1</info> | <info descr="null">param2</info> |
  <error descr="Row must have the same cells count as table's header">| 1      |</error>
  Examples:
    | <info descr="null">param1</info> | <info descr="null">param2</info> |
  <error descr="Row must have the same cells count as table's header">| 1      | 2      | 3 |</error>

