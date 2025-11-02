Feature: Some interesting feature

  Scenario: Consumption of a cookie
    Given <warning descr="Ambiguous step reference with 2 definitions: the step is my_path">the step is <info descr="null">my_path</info></warning>
    Given <warning descr="Ambiguous step reference with 3 definitions: another step is my_path blah!">another step is <info descr="null">my_path</info> blah!</warning>
    And this step is very unambiguous
    And <info descr="null">current</info> branch <info descr="null">foo</info>
    When <info descr="null">current</info> branch <info descr="null">bar</info>
