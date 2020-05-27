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
 * quick-doc for ## comments on target line
 * find usages and Go To Symbol navigation for targets
 * prerequisites resolution
 * rules, variables and conditionals folding
 * quick fixes to create new rule from unresolved prerequisite and remove empty rule

![](https://victor.kropp.name/projects/makefile/makefile-example.png)

### Development

The plugin is built using Gradle and uses [gradle-intellij-plugin](https://github.com/JetBrains/gradle-intellij-plugin)
to integrate with IntelliJ Platform.
 
To build a plugin run

```
$ ./gradlew buildPlugin
```

Plugin zip file will be created in `build/distributions`

To test plugin in IDE run `./gradlew runIde`

#### Grammar modifications

The plugin uses [Grammar-Kit](https://github.com/jetbrains/grammar-kit) to generate parser and lexer. Please install [Grammar-Kit plugin](https://plugins.jetbrains.com/plugin/6606-grammar-kit) and refer to the documentation if you want to modify grammar.

To regenerate parser, open Makefile.bnf and press Ctrl+Shift+G (Cmd+Shift+G on Mac)
To regenerate lexer, open Makefile.flex and press Ctrl+Shift+G (Cmd+Shift+G on Mac)

Please make sure to add test to MakefileParserTest.kt for any parser modifications.
 
### Contribution

Plugin is written in [Kotlin](http://kotlinlang.org/).

[GNU head icon](https://www.gnu.org/graphics/heckert_gnu.html) licensed under CC-BY-SA 2.0
