Feature: Basic Arithmetic

  Scenario: Addition
    And step with quotes "" and another quotes "<info descr="null">value</info>" on test

  Scenario Outline:
    When there are <info descr="null"><<info descr="null">count</info>></info> open sheets for <info descr="null">Form-TP-002</info> created by <info descr="null">Ivan</info>
    Examples:
      | <info descr="null">count</info> |
      | 0                               |

  Scenario Outline: Some title 1
    Given I expect inspection warning on <info descr="null"><<info descr="null">type</info>></info> with messages 1
    Examples:
      | <info descr="null">type</info>   |
      | class  |
      | method |
      | field  |

  Scenario Outline: Some title 2
    Given I expect inspection warning on <<info descr="null"><<info descr="null">type</info>></info>> with messages 2
    Examples:
      | <info descr="null">type</info>   |
      | class  |
      | method |
      | field  |

  Scenario Outline: Some title 3
    Given I expect inspection warning on <<<info descr="null"><<info descr="null">type</info>></info>>> with messages 3
    Examples:
      | <info descr="null">type</info>   |
      | class  |
      | method |
      | field  |

  Scenario Outline: Double caret test
    Given my another step definition with param <info descr="null">"<<<info descr="null">param</info>>>"</info>
    Examples:
      | <info descr="null">param</info> |
      | hello |
      | there |