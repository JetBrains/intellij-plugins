My Specification Heading
=====================

tags: first, second

This is an executable specification file. This file follows markdown syntax. Every heading in this file denotes a scenario. Every bulleted point denotes a step.

To execute this specification, use

	gauge spec/hello_world.spec

* A context step which gets executed before every scenario

First scenario
--------------

Tags: hello world, first test

* Say "hello" to "gauge"
* another one with "ss" and "sss"
* another one with "ss" and "sss"



## Second scenario

This is the second scenario in this specification

* another one with "ss" and "sss"
* a new step


* Say "hello again" to "gauge"
* Step that takes a table
    |Product|       Description           |
    |-------|-----------------------------|
    |Gauge  |BDD style testing with ease  |
    |Mingle |Agile project management     |
    |Snap   |Hosted continuous integration|
    |Gocd   |Continuous delivery platform |
