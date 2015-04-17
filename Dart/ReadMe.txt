How to start working with this 'Dart-plugin' project.

Prerequisites:
- Oracle JDK 1.6 or later
- git command line client

1. Install the latest IntelliJ IDEA Ultimate Edition. The latest is either official release (https://www.jetbrains.com/idea/download/) or (more likely) Early Access build (http://eap.jetbrains.com/idea). Keep IDE installation up-to-date. Make sure the following bundled plugins are enabled: UI Designer, Git Integration, I18n for Java, IntelliLang, JUnit, Plugin DevKit, Properties Support.
2. [Optional] If you are going to debug the plugin in WebStorm install the latest WebStorm, start IntelliJ IDEA, open Settings (on Mac: Preferences) | Path Variables and add WEBSTORM_PLUGINS pointing to [WebStorm Installation]/plugins (on Mac: [WebStorm Installation]/Contents/plugins)
3. Install 'Grammar-Kit' and 'Polymer & Web Components' plugins: launch IntelliJ IDEA, on Welcome screen click Configure | Plugins. Press 'Browse repositories...', find plugins and install them. Restart IDE.
4. Configure JDK and Plugin SDK.
  4.1. Launch IntelliJ IDEA, on Welcome screen click Configure | Project Defaults | Project Structure | SDKs, click [+] and add JDK (1.6 or 1.7), then click [+] again, select 'IntelliJ Platform Plugin SDK' and provide path to the current IntelliJ IDEA installation.
  4.2. Rename Plugin SDK to 'IDEA Ultimate'.
  4.3. Add some jars to the Plugin SDK Classpath:
         [IDEA Installation]/plugins/copyright/lib/*.jar
         [IDEA Installation]/plugins/JavaScriptDebugger/lib/*.jar
         [IDEA Installation]/plugins/CSS/lib/*.jar
         [IDEA Installation]/plugins/yaml/lib/*.jar
         [IDEA Plugins folder]/WebComponents/lib/*.jar (see here how to locate [IDEA Plugins folder]: https://intellij-support.jetbrains.com/entries/23358108)
  4.4. Clone IntelliJ IDEA Community Edition repo (git clone https://github.com/JetBrains/intellij-community) anywhere on the computer (it takes some time), open Sources tab of the Plugin SDK and add path to the cloned repo root. After some scanning IDE will find all src folders - accept them. To have IntelliJ IDEA Community sources matching IntelliJ IDEA Ultimate installation it is recommended to update Community repo (git pull) at the day when Ultimate is released and switch Community repo to the branch that corresponds to the Ultimate installation.
  4.5. Open Plugin SDK Annotations tab and add [IDEA Installation]/lib/jdkAnnotations.jar
5. Clone IntelliJ IDEA plugins source code (https://github.com/JetBrains/intellij-plugins) and open the project in the 'Dart' folder. It is ready to use. Two run configurations are already configured: 'All tests' (run it each time before commit) and 'Dart plugin' that launches IntelliJ IDEA with the Dart plugin built from sources. Important note: to have all tests passing successfully uncomment lines with <depends>...</depends> tags in the resources/META-INF/plugin.xml file. Do not commit plugin.xml with uncommented <depends/> tags!
6. [Optional] To enable internal developer actions add "idea.is.internal=true" to the [IDE installation]/bin/idea.properties (on Mac: [IDE installation]/Contents/bin/idea.properties) file. The menu actions Tools -> View PSI Structure... as well as the Tools -> Internal Actions should be visible after restarting.
7. [Optional] To run WebStorm with Dart plugin built from sources: Build | Build Artifacts | All artifacts and then manually start WebStorm
8. [Optional] To debug Dart plugin in WebStorm:
  8.1. First way (single click but makes WebStorm impossible to run without debugging):
    - create Remote run configuration, select Mode: Listen, add both artifacts in correct order to the Before Launch section.
    - add the line (starting from -agentlib...) shown in the Remote RC to the [WebStorm]/bin/idea.properties (on Mac: [WebStorm]/Contents/bin/idea.properties) file
    - now to debug WebStorm with Dart you simply click Debug for this RC and manually launch WebStorm
  8.2. Second way to debug (two clicks but WebStorm remains runnable without debugging):
    - create Remote run configuration, leave default Mode: Attach, nothing in Before Launch
    - add the line (starting from -agentlib...) shown in the Remore RC to [WebStorm]/Contents/bin/idea.properties file
    - now to debug WebStorm you:
      - Build | Build Artifacts | All artifacts
      - manually run WebStorm
      - click Debug for this RC
