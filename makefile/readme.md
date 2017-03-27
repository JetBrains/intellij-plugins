Makefile support for IntelliJ-based IDEs
========================================

Get it from plugin repository: https://plugins.jetbrains.com/plugin/9333-makefile-support

Plugin for editing Makefiles in IntelliJ-based IDEs.

Fully supports GNU Make syntax.

Provides:
 * syntax highlighting
 * keywords & target names completion
 * run configurations
 * gutter marks & context actions to run targets
 * find usages and Go To Symbol navigation for targets
 * prerequisites resolution
 * rules, variables and conditionals folding
 * quick fixes to create new rule from unresolved prerequisite and remove empty rule

<img src="https://victor.kropp.name/projects/makefile/makefile-example.png" width="600">

### Development

The plugin is built using Gradle and uses [gradle-intellij-plugin](https://github.com/JetBrains/gradle-intellij-plugin)
to integrate with IntelliJ Platform.
 
To build a plugin run

```
$ ./gradlew buildPlugin
```

Plugin zip file will be created in `build/distributions`

To test plugin in IDE run `./gradlew runIdea`
 
### Contribution

Plugin is written in [Kotlin](http://kotlinlang.org/).

[GNU head icon](https://www.gnu.org/graphics/heckert_gnu.html) licensed under CC-BY-SA 2.0
