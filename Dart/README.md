# Dart Plugin for IntelliJ IDEA and other JetBrains IDEs

This plugin provides support for the [Dart programming language](https://dart.dev/).

## Installing and Getting Started

See the
[plugin documentation](https://www.jetbrains.com/help/idea/dart.html)
for help to get started with Dart development and the IntelliJ IDEA plugin. 

## Reporting Issues

Please file issues at YouTrack
[bug tracker](https://youtrack.jetbrains.com/issues/WEB?q=Subsystem:%20Dart).

## Developing the Plugin

How to set up IntelliJ IDEA project for developing Dart Plugin.

Prerequisites:
- Git command line client
- The latest IntelliJ IDEA Ultimate installed. Ensure the following installed plugins are enabled:
  Git, Kotlin, Java Internationalization, IntelliLang, JUnit, Plugin DevKit, Properties, UI Designer.
  Additionally install 'Dart' and 'Grammar-Kit' plugins:
  start IntelliJ IDEA, on Welcome screen click Plugins.
  Find plugins and install them. Restart IDE.

1. Clone the following 2 repositories to neighbor folders (on Windows use a short path,
   as there is a maximum allowed length):
     - `git clone https://github.com/JetBrains/intellij-plugins`
     - `git clone https://github.com/JetBrains/intellij-community`
     
   Run `getPlugins.sh` (`getPlugins.bat` on Windows) from `intellij-community/`. It will clone
   `https://github.com/JetBrains/android.git` repository to the `intellij-community/android` folder.
   If it's already cloned, just update it using `git pull`.

2. Open and build the `intellij-community` project as described in its 
   [README file](https://github.com/JetBrains/intellij-community/blob/master/README.md#building-intellij-community-edition).
   
3. Open File | Project Structure | Modules | `[+]` | Import Module, select `intellij-plugins/Dart/Dart-community.iml`.
   In the same Project Structure dialog open the Dependencies tab of the `intellij.idea.community.main` module
   (`intellij` > `idea` > `community` > `main`), click `[+]` and add a module
   dependency on the `Dart-community` module.

4. Open Settings (Preferences) | Version Control | Directory Mappings and add `intellij-plugins` as a 3rd Git root if it's not already there.

5. The project is ready to use. Use `IDEA` run configuration to start IntelliJ IDEA Community Edition + Dart Plugin from sources.

6. In order to be able to run tests that use real Dart Analysis Server, do the following:
   - Make sure you have Dart SDK installed locally. It can be downloaded from the [Dart SDK Archive](https://dart.dev/tools/sdk/archive).
   - Open `Run | Edit Configurations...` dialog.
   - Click `Edit configuration templates...` in the bottom-left corner.
   - Find `JUnit` and add Dart SDK path to the VM Options field like this: `-Ddart.sdk=path/to/real/dart/sdk`. 
     VM Options field is the one that has only `-ea` text by default.

7. To run all Dart plugin tests, right-click the `Dart/testSrc` folder in the Project View and select 'Run all tests'.
   - To run only those tests that do not use real Dart Analysis Server, use context menu of the `Dart/testSrc/com/jetbrains/lang/dart` folder.
     (At the moment of writing, 3 of them are expected to fail: `DartInjectionTest.testRegExp()`, `DartHighlightingTest.testSimplePolymer()`, 
     and `DartHighlightingTest.testScriptSrcPathToPackagesFolder()`.)
   - Tests that use real Dart Analysis Server reside in the `Dart/testSrc/com.jetbrains.dart/analysisServer` folder.
     A few of them are expected to fail.

8. [Optional] To enable internal developer actions add "idea.is.internal=true"
   to the idea.properties file (Help -> Edit Custom Properties...). The menu actions Tools ->
   View PSI Structure... as well as the Tools -> Internal Actions should be
   visible after restarting.

9. Enjoy! All functionality should work. Most of the tests should pass.
   Zero mandatory local changes in `intellij-plugins` repository.
   There should be 3 locally changed files in `intellij-community` repository, each having exactly one added line,
   just keep these files in a separate '~never commit' changelist and do not worry about them:
     - intellij-community/intellij.idea.community.main.iml (line `<orderEntry type="module" module-name="Dart-community" />`)
     - intellij-community/.idea/modules.xml (line `<module fileurl="file://$PROJECT_DIR$/../intellij-plugins/Dart/Dart-community.iml" ... />`)
     - intellij-community/.idea/vcs.xml (line `<mapping directory="$PROJECT_DIR$/../intellij-plugins" vcs="Git" />`)

### Contributing to the Dart Plugin

All contributed PRs should:

  - be formatted with the Java formatter (Code -> Reformat Code)
  - not introduce new warnings reported by the IDE
  - not break any of the tests that were passing before your changes
  - if possible, additional tests for the change in functionality should be included

### How to open the Dart Analysis Server with Observatory and Dart DevTools

1. Open the Registry... dialog by opening Find Action, `Ctrl + Shift + A` and typing `Registry...`
2. Find the key `dart.server.vm.options` and include the flags: `--disable-service-auth-codes --observe=8888`
3. Restart the Dart Analysis Server by clicking on the `Restart Dart Analysis Server` button in the Analysis window
4. Open a [Observatory](https://dart-lang.github.io/observatory/) browser at [`http://localhost:8888/`](http://localhost:8888/)
5. To open [Dart Devtools](https://flutter.dev/docs/development/tools/devtools/overview), copy the `ws://localhost:8888/ws` portion of
   `vm@ws://localhost:8888/ws` out of the `name` field at the top of Observatory.
6. If you haven't already install DevTools with `pub global activate devtools`, the Dart SDK must be `2.3.0` or higher
7. Launch DevTools with `devtools`, when prompted paste in the `ws://localhost:8888/ws` contents
