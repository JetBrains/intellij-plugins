# Spec with data table

tags: first, second

|first|second |   third  |
|111  | 33    |    hello |
|122  | 344   |    world |

* execute this <first> before every scenario


My First scenario
--------------

Tags: hello world, first test

* First step with <first>
* second step with a param <second>



## Second scenario

* another one with "ss" and "sss"

This is the second scenario in this specification

* Step that takes a table
    |Product|       Description           |
    |-------|-----------------------------|
    |Gauge  |BDD style testing with ease  |
    | <third> |Agile project management   |
    |Snap   |Hosted continuous integration|

* a new step