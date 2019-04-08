# Grazi

[![CircleCI](https://circleci.com/gh/TanVD/Grazi.svg?style=svg)](https://circleci.com/gh/TanVD/Grazi)

Grazi is IntelliJ IDEA plugin providing local spell and grammar checking for Markdown, JavaDoc, Plain texts and others.

It uses one of the leading proofreaders - [LanguageTool](https://github.com/languagetool-org/languagetool)
under the hood, so it supports over 15 languages and provides the best performance and 
accuracy among free (and even non-free) alternatives.

Basically, *Grazi is a Grammarly inside your IDE*. 

## What's inside

Grazi consists of two parts - grammar checker, backed by LanguageTool, and spellchecker backed by
LanguageTool dictionaries and IDEA built-in spellcheck. When it is sensible, Grazi will use full
checking pipeline (proofreading and spellchecking), but in some cases (e.g. in code) it will 
use only spellcheck.

Right now Grazi supports following natural language sources:
* Java code - string literals, javadocs and language constructs (methods names etc.)
* Kotlin code - string literals, kdoc and language constructs
* Python code - string literals (formatted and non-formatted), python docs and language constructs
* Markdown - all the text (for code Grazi will use spellcheck only)
* Plaintext - all the text if extension is *.txt*, otherwise Grazi will use spellcheck only
* XML - all the text elements
* Comments - any comments in almost any code 

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

Moreover, Grazi supports *native language based* inspections. It means, that if you will specify
your native language, Grazi will provide you with additional inspections for language you are
writing on.

## Setup

For local development and testing Gradle is used:

* Import project as a Gradle project with IntelliJ IDEA (version `2018.3.+`)
* Run `runIde` task to run IntelliJ IDEA Community (downloaded by Gradle) 
  with installed Grazi plugin from current build

