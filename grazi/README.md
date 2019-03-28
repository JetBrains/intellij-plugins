# Grazi

[![CircleCI](https://circleci.com/gh/TanVD/Grazi.svg?style=svg)](https://circleci.com/gh/TanVD/Grazi)

Grazi is IntelliJ IDEA plugin providing local spell and grammar checking for Markdown, JavaDoc, Plain texts and others.

It uses one of the leading proofreaders &mdash; [LanguageTool](https://github.com/languagetool-org/languagetool)
under the hood, so it supports more than 20 languages and  provides best performance and 
accuracy among free (and even non-free) alternatives.

Basically, *Grazi is a Grammarly inside your IDE*. 

## Setup

For local development and testing Gradle is used:

* Import project as a Gradle project with IntelliJ IDEA (ver `2018.3.+`)
* Run `runIde` task to run IntelliJ IDEA Community (downloaded by Gradle) 
  with installed Grazi plugin from current build

