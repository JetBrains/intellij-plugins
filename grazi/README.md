<h1> <img align="left" width="40" height="40" src="https://plugins.jetbrains.com/files/12175/63853/icon/pluginIcon.svg" alt="Grazi Icon"> Grazi </h1>

[![JetBrains Plugin](https://img.shields.io/jetbrains/plugin/v/12175-grazi.svg?style=flat-square&label=jetbrains%20plugin)](https://plugins.jetbrains.com/plugin/12175-grazi)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/12175-graz.svg?style=flat-square)](https://plugins.jetbrains.com/plugin/12175-grazi)
[![CircleCI](https://img.shields.io/circleci/build/github/TanVD/Grazi.svg?style=flat-square)](https://circleci.com/gh/TanVD/Grazi)
[![Gitter](https://img.shields.io/gitter/room/grazi-intellij-plugin/community.svg?style=flat-square)](https://gitter.im/grazi-intellij-plugin/community?utm_source=share-link&utm_medium=link&utm_campaign=share-link)


Grazi is an IntelliJ IDEA plugin providing local spell and grammar checking for Markdown, JavaDoc, Plain texts, and others.

It uses one of the leading proofreaders - [LanguageTool](https://github.com/languagetool-org/languagetool)
under the hood, so it supports over 15 languages and provides the best performance and 
accuracy among free (and even non-free) alternatives.

In general, *Grazi is a Grammarly inside your IDE*. 

![Usage of Grazi plugin](https://gph.is/g/ZdxKVA3)

## What's inside

Grazi consists of two parts - grammar checker, backed by LanguageTool, and spellchecker backed by LanguageTool dictionaries and IDEA built-in spellcheck. When it is sensible, Grazi will use full checking pipeline (proofreading and spellchecking), but in some cases (e.g., in code) it will use just spellcheck.

Right now Grazi supports following natural language sources:
* Java code - string literals, javadocs and language constructs (methods names etc.)
* Kotlin code - string literals, kdoc and language constructs
* Python code - string literals (formatted and non-formatted), python docs and language constructs
* JavaScript code - string literals, docs and language constructs
* Rust code - string literals, docs and language constructs
* Latex - (via TeXiFy IDEA plugin) text, spellcheck
* Markdown - all the text (for code Grazi will use spellcheck only)
* Plaintext - all the text if extension is *.txt*, otherwise Grazi will use spellcheck only
* XML - all the text elements
* JSON - string literals
* Properties - string literals
* Comments - any comments in almost any code 
* Commit messages - commits made via standard IDEA VCS support

As for languages, Grazi supports (including dialects):
* English (British, American, Canadian)
* Russian
* Persian
* French
* German (Germany, Austrian)
* Polish
* Italian
* Dutch
* Portuguese (Brazilian, Portugal)
* Chinese
* Greek
* Japanese
* Romanian
* Slovak
* Spanish
* Ukrainian

Moreover, Grazi supports *native language based* inspections. It means that if you specify your native language, Grazi will provide you with additional inspections for the language you are writing on.

## Setup

For local development and testing Gradle is used:

* Import project as a Gradle project with IntelliJ IDEA (version `2018.3.+`)
* Run `runIde` task to run IntelliJ IDEA Community (downloaded by Gradle) 
  with installed Grazi plugin from current build
  
## Special thanks
Special thanks goes to:
* Alexandra Pavlova (aka sunalex) for our beautiful icon
* Alexandr Sadovnikov and Nikita Sokolov as one of initial developers of plugin


