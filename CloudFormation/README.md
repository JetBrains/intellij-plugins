AWS CloudFormation plugin for IntelliJ IDEA 16+

[![Travis Build Status](https://travis-ci.org/shalupov/idea-cloudformation.svg?branch=master)](https://travis-ci.org/shalupov/idea-cloudformation)

Latest stable version
---------------------

See IntelliJ IDEA plugin repository:
https://plugins.jetbrains.com/plugin/7371?pr=idea_ce

Quick Start
-----------

* Open any *.template or *.json file with CloudFormation JSON inside
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

Builds from trunk
-----------------

https://teamcity.jetbrains.com/project.html?projectId=IdeaAwsCloudFormation_Master&guest=1

Build and Test locally
----------------------

 * "gradlew assemble" to build plugin in build/distributions
 * "gradlew test" to run tests
 * "gradlew runIdea" to run IntelliJ IDEA with CloudFormation plugin
