AWS CloudFormation plugin for IntelliJ-based IDEs  
(IntelliJ IDEA, RubyMine, WebStorm, PhpStorm, PyCharm, AppCode, Android Studio, DataGrip, CLion)

This plugin was originally developed there: https://github.com/shalupov/idea-cloudformation

Report issues to: [YouTrack](https://youtrack.jetbrains.com/issues/IDEA?q=%23%7BLang.%20CloudFormation%7D%20)

Latest stable version
---------------------

Install plugin from IDE: File -> Settings -> Plugins -> Browse Repositories -> search for CloudFormation

See IntelliJ IDEA plugin repository:
https://plugins.jetbrains.com/plugin/7371

Quick Start
-----------

Note: you need IDEA 2017.1 or later for YAML support

* Open any *.template, *.json, *.yaml file with CloudFormation JSON/YAML inside
* There should be number of features available:
  * Template validation
    * Overall file structure
    * References to resources, conditions, parameters, mappings
    * Resource types and properties
  * File structure (aka Go to member) (Ctrl-F12 on Windows): fast jump to any entity in the file
  * Completion in Ref clause
  * Completion of resources types and properties
  * Live template for Ref clause: type "ref" and press Tab
  * Ctrl-Click on any reference to jump to its definition
  * Quick Documentation for resource types and properties
  * Format file
