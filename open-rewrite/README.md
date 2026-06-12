# OpenRewrite Plugin for IntelliJ IDEA-based IDEs

This plugin provides support for [OpenRewrite](https://docs.openrewrite.org/) in IntelliJ IDEA and
other IntelliJ Platform IDEs. OpenRewrite is an automated refactoring ecosystem for source code that
applies "recipes" to perform large-scale, type-aware code migrations.

The plugin adds editing support for OpenRewrite YAML recipe files (completion, navigation,
inspections, documentation and inline hints) and lets you run recipes through your project's build
system, with dedicated integrations for Gradle, Maven, Micronaut, Quarkus and Spring Boot.

## Building

This module is part of the [IntelliJ IDEA Community](https://github.com/JetBrains/intellij-community)
monorepo and is built together with the IDE. It can also be built as a standalone plugin with the
bundled Gradle wrapper:

```
./gradlew buildPlugin
```

The standalone build compiles the core plugin together with all of its build-system integration
modules and requires an IntelliJ IDEA Ultimate distribution, which the
[IntelliJ Platform Gradle Plugin](https://plugins.jetbrains.com/docs/intellij-platform-gradle-plugin/)
resolves automatically.

## Reporting issues

Please report any issues on https://youtrack.jetbrains.com/issues/IDEA.
