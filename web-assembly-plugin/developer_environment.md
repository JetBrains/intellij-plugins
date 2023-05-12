
#### Setting up a development environment

* Fork this project, clone it to your machine
* Launch IDEA and open the `web-assembly-plugin` directory using `File -> New -> Project from Existing Sources...`. Select 'Gradle' and accept the default settings.

* At this point you should be ready to go! To **launch IDEA** with the plugin built from sources use `runIde` Gradle task (from the IDEA UI search for 'Execute Gradle Task' or run `./gradlew runIde` from the command line).

The project structure and dependencies are defined in [build.gradle](./build.gradle).

**Useful links**
* [gradle-intellij-plugin](https://github.com/JetBrains/gradle-intellij-plugin) documentation on available Gradle tasks and build.gradle configuration options
* [IDEA SDK documentation](https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started.html) describes IDEA plugin development in general
