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
  Additionally install 'Dart', 'Grammar-Kit' and 'Polymer & Web Components' plugins:
  start IntelliJ IDEA, on Welcome screen click Plugins.
  Find plugins and install them. Restart IDE.

1. Clone the following 2 repositories to neighbor folders (on Windows use a short path,
   as there is a maximum allowed length):
     - `git clone https://github.com/JetBrains/intellij-plugins`
     - `git clone https://github.com/JetBrains/intellij-community`
     
   Run `getPlugins.sh` (`getPlugins.bat` on Windows) from `intellij-community/`. It will clone
   `https://github.com/JetBrains/android.git` repository to the `intellij-community/android` folder.
   If it's already cloned, just update it using `git pull`.

2. On Welcome screen click Customize, All settings..., look for Path Variables and add the `IDEA_ULTIMATE_PLUGINS`
   variable pointing to `[IntelliJ IDEA Ultimate Installation]/plugins` (Linux, Windows) or 
   `[IntelliJ IDEA Ultimate Installation]/Contents/plugins` (Mac)

3. Open and build the `intellij-community` project as described in its 
   [README file](https://github.com/JetBrains/intellij-community/blob/master/README.md#building-intellij-community-edition).
   
4. Open File | Project Structure | Modules | `[+]` | Import Module, select intellij-plugins/Dart/Dart-community.iml.
   In the same Project Structure dialog open the Dependencies tab of the `intellij.idea.community.main` module
   (`intellij` > `idea` > `community` > `main`), click `[+]` and add a module
   dependency on the `Dart-community` module.

5. Open Settings (Preferences) | Version Control | Directory Mappings and add `intellij-plugins` as a 3rd Git root if it's not already there.

6. The project is ready to use. There are 2 preconfigured run configurations for Dart plugin developers:
   - `IDEA` - to start IntelliJ IDEA + Dart Plugin from sources,
   - `Dart tests` - it runs all JUnit tests in the Dart plugin module

7. Important! In order to be able to run a single test class or test method you need to do the following:
   - open Run | Edit Configurations, select 'Dart tests' run configuration, copy its VM Options to clipboard
   - in the same dialog (Run/Debug Configurations) expand Defaults node, find JUnit under it and paste VM Options
     to the corresponding field
   - repeat the same with Working directory field - it must point to intellij-community/bin

8. [Optional] To enable internal developer actions add "idea.is.internal=true"
   to the idea.properties file (Help -> Edit Custom Properties...). The menu actions Tools ->
   View PSI Structure... as well as the Tools -> Internal Actions should be
   visible after restarting.

9. Enjoy! All tests should pass. All functionality should work.
   Zero mandatory local changes in `intellij-plugins` repository.
   There should be 3 locally changed files in `intellij-community` repository, each having exactly one added line,
   just keep these files in a separate '~never commit' changelist and do not worry about them:
     - intellij-community/intellij.idea.community.main.iml (line `<orderEntry type="module" module-name="Dart-community" />`)
     - intellij-community/.idea/modules.xml (line `<module fileurl="file://$PROJECT_DIR$/../intellij-plugins/Dart/Dart-community.iml" ... />`)
     - intellij-community/.idea/vcs.xml (line `<mapping directory="$PROJECT_DIR$/../intellij-plugins" vcs="Git" />`)
   
   To run the `analysisServer` tests add `-Ddart.sdk=/absolute/path/to/dart-sdk` to the `VM options` field in the launch configuration.
   Dart SDK versions can be downloaded from the [Dart SDK Archive](https://dart.dev/tools/sdk/archive).

### Contributing to the Dart Plugin

All contributed PRs should:

  - be formatted with the Java formatter (Code -> Reformat Code)
  - not introduce new warnings reported by the IDE
  - not break any of the tests mentioned above
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
