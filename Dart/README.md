# Dart Plugin for IntelliJ IDEA and other JetBrains IDEs

This plugin provides support for the [Dart programming language](https://www.dartlang.org/).

## Installing and Getting Started

See the
[plugin documentation](https://www.jetbrains.com/help/idea/dart-support.html)
for help getting started with Dart development and the IntelliJ IDEA plugin. 

## Reporting Issues

Please file issues at YouTrack
[bug tracker](https://youtrack.jetbrains.com/issues/WEB?q=Subsystem:%20Dart).

## Developing the Plugin

How to setup IntelliJ IDEA project for developing Dart Plugin.

Prerequisites:
- Oracle JDK 1.8
- Git command line client
- IntelliJ IDEA Ultimate installed. The following bundled plugins are enabled:
  Git Integration, I18n for Java, IntelliLang, JUnit, Plugin DevKit, Properties Support, UI Designer.
  Additionally install 'Dart', 'Grammar-Kit' and 'Polymer & Web Components' plugins:
  start IntelliJ IDEA, on Welcome screen click Configure | Plugins.
  Press 'Browse repositories...', find plugins and install them. Restart IDE.

1. Clone the following 2 repositories to neighbor folders:
     - git clone https://github.com/JetBrains/intellij-plugins,
     - git clone https://github.com/JetBrains/intellij-community,
   Run intellij-community/getPlugins.sh (getPlugins.bat on Win). If that fails because the
   repos have already been cloned you can just update both of them using 'get pull'.

2. Start IntelliJ IDEA Ultimate, on Welcome screen click Configure | Project Defaults | Project Structure | SDKs,
   click [+] and add JDK 1.8. Add [JDK]/lib/tools.jar to the SDK Classpath if it is not there. Rename SDK to 'IDEA jdk'.
   Click [+] to add one more JDK and provide path to JDK 1.8 once again . Leave default name "1.8". Make sure it contains [JDK]/lib/tools.jar

3. On Welcome screen click Configure | Settings (Preferences), look for Path Variables and add the following vars there:
   - IDEA_ULTIMATE_PLUGINS pointing to [IntelliJ IDEA Ultimate Installation]/Contents/plugins
     (on Windows: [IntelliJ IDEA Ultimate Installation]/plugins)

4. Open intellij-community project, compile it.
   Open File | Project Structure | Modules | [+] | Import Module, select intellij-plugins/Dart/Dart-community.iml.
   In the same Project Structure dialog open the Dependencies tab of the community-main module,
   click [+] at the bottom (Mac) or right (Win/Linux) to add a module dependency on the Dart-community module.

5. Open Settings (Preferences) | Version Control and make sure that intellij-plugins is configured as a 4th Git root.
   If you previously worked with a single-module Dart-plugin project you may have unversioned
   intellij-plugins/Dart/.idea/workspace.xml file and intellij-plugins/Dart/out folder. You may simply delete these files
   or add to Settings (Preferences) | Version Control | Ignored Files.

6. Project is ready to use. There are 2 preconfigured run configurations for Dart plugin developers:
   IDEA - to start IntelliJ IDEA + Dart Plugin from sources,
   Dart tests - it runs all JUnit tests in the Dart plugin module

7. Important! In order to be able to run a single test class or test method you need to do the following:
   - open Run | Edit Configurations, select 'Dart tests' run configuration, copy its VM Options to clipboard
   - in the same dialog (Run/Debug Configurations) expand Defaults node, find JUnit under it and paste VM Options
     to the corresponding field
   - repeat the same with Working directory field - it must point to intellij-community/bin

8. [Optional] To enable internal developer actions add "idea.is.internal=true"
   to the [idea.properties](https://www.jetbrains.com/help/idea/file-idea-properties.html) file. The menu actions Tools ->
   View PSI Structure... as well as the Tools -> Internal Actions should be
   visible after restarting.

9. Enjoy! All tests should pass. All functionality (except debugging in browser) should work.
   Zero mandatory local changes in intellij-plugins repository.
   Only 3 files locally changed in intellij-community repository, just keep them in a separate '~never commit' changelist
   and do not worry about them:
     - intellij-community/community-main.iml
     - intellij-community/.idea/modules.xml
     - intellij-community/.idea/vcs.xml

---

## Alternative way of the Dart-plugin project setup

1. Install the latest IntelliJ IDEA Ultimate Edition. The latest is either
official release (https://www.jetbrains.com/idea/download/) or (more likely)
Early Access build (http://eap.jetbrains.com/idea). Keep IDE installation
up-to-date. Make sure the following bundled plugins are enabled: UI Designer,
Git Integration, I18n for Java, IntelliLang, JUnit, Plugin DevKit, Properties
Support.

2. [Optional] If you are going to debug the plugin in WebStorm install the
latest WebStorm, start IntelliJ IDEA, open Settings (on Mac: Preferences) |
Path Variables and add WEBSTORM_PLUGINS pointing to [WebStorm
Installation]/plugins (on Mac: [WebStorm Installation]/Contents/plugins)

3. Install 'Grammar-Kit' and 'Polymer & Web Components' plugins: launch
IntelliJ IDEA, on Welcome screen click Configure | Plugins. Press 'Browse
repositories...', find plugins and install them. Restart IDE.

4. Configure JDK and Plugin SDK.

  4.1. Launch IntelliJ IDEA, on Welcome screen click Configure | Project
  Defaults | Project Structure | SDKs, click [+] and add JDK 1.8,
  then click [+] again, select 'IntelliJ Platform Plugin SDK' and provide path
  to the current IntelliJ IDEA installation.

  4.2. Rename Plugin SDK to 'IDEA Ultimate'.

  4.3. Add some jars to the Plugin SDK Classpath:
    - [IDEA Installation]/plugins/coverage/lib/\*.jar
    - [IDEA Installation]/plugins/copyright/lib/\*.jar
    - [IDEA Installation]/plugins/JavaScriptDebugger/lib/\*.jar
    - [IDEA Installation]/plugins/JavaScriptLanguage/lib/\*.jar
    - [IDEA Installation]/plugins/CSS/lib/\*.jar
    - [IDEA Installation]/plugins/yaml/lib/\*.jar
    - [IDEA Plugins folder]/WebComponents/lib/\*.jar (see [here](https://intellij-support.jetbrains.com/entries/23358108) how to locate [IDEA Plugins folder])

  4.4. Clone IntelliJ IDEA Community Edition repo (git clone
  https://github.com/JetBrains/intellij-community) anywhere on the computer
  (it takes some time), open Sources tab of the Plugin SDK and add path to the
  cloned repo root. After some scanning IDE will find all src folders - accept
  them. To have IntelliJ IDEA Community sources matching IntelliJ IDEA
  Ultimate installation it is recommended to update Community repo (git pull)
  at the day when Ultimate is released and switch Community repo to the branch
  that corresponds to the Ultimate installation.

  4.5. Open Plugin SDK Annotations tab and add [IDEA
  Installation]/lib/jdkAnnotations.jar

5. Clone IntelliJ IDEA plugins source code
(https://github.com/JetBrains/intellij-plugins) and open the project in the
'Dart' folder. It is ready to use. Four run configurations are already
configured: 3 ones for running tests and 'Dart plugin' that launches IntelliJ
IDEA with the Dart plugin built from sources.

6. [Optional] To enable internal developer actions add "idea.is.internal=true"
to the [idea.properties](https://www.jetbrains.com/help/idea/file-idea-properties.html) file. The menu actions Tools ->
View PSI Structure... as well as the Tools -> Internal Actions should be
visible after restarting.

7. [Optional] To run WebStorm with Dart plugin built from sources: Build |
Build Artifacts | All artifacts and then manually start WebStorm

8. [Optional] To debug Dart plugin in WebStorm:

  8.1. First way (single click but makes WebStorm impossible to run
  without debugging):
    - create Remote run configuration, select Mode: Listen, add both
      artifacts in correct order to the Before Launch section.
    - add the line (starting from -agentlib...) shown in the Remote RC to
      the WebStorm's [idea.properties](https://www.jetbrains.com/help/idea/file-idea-properties.html) file
    - now to debug WebStorm with Dart you simply click Debug for this RC
      and manually launch WebStorm

  8.2. Second way to debug (two clicks but WebStorm remains runnable
  without debugging):
    - create Remote run configuration, leave default Mode: Attach, nothing
      in Before Launch
    - add the line (starting from -agentlib...) shown in the Remote RC
      to the WebStorm's [idea.properties](https://www.jetbrains.com/help/idea/file-idea-properties.html) file
    - now to debug WebStorm you:
      - Build | Build Artifacts | All artifacts
      - manually run WebStorm
      - click Debug for this RC
