How to start working with this 'Dart-plugin' project.

Prerequisites:
- Oracle JDK 1.6 or later
- git command line client

1. Install the latest IntelliJ IDEA Ultimate Edition. The latest is either official release (http://www.jetbrains.com/idea/download/) or (more likely) Early Access build (http://eap.jetbrains.com/idea). Keep IDE installation up-to-date. Make sure the following bundled plugins are enabled: UI Designer, Git Integration, I18n for Java, IntelliLang, JUnit, Plugin DevKit, Properties Support.
2. Install 'Grammar-Kit' plugin: launch IntelliJ IDEA, on Welcome screen click Configure | Plugins. Press 'Browse repositories...', find 'Grammar-Kit' plugin and install it. Restart IDE.
3. Configure JDK and Plugin SDK.
  3.1. Launch IntelliJ IDEA, on Welcome screen click Configure | Project Defaults | Project Structure | SDKs, click [+] and add JDK (1.6 or 1.7), then click [+] again, select 'IntelliJ Platform Plugin SDK' and provide path to the current IntelliJ IDEA installation.
  3.2. Rename Plugin SDK to 'IDEA Ultimate'.
  3.3. Add some jars to the Plugin SDK Classpath:
         [IDEA Installation]/plugins/copyright/lib/*.jar
         [IDEA Installation]/plugins/JavaScriptDebugger/lib/*.jar
         [IDEA Installation]/plugins/htmltools/lib/*.jar
         [IDEA Installation]/plugins/CSS/lib/*.jar
  3.4. Clone IntelliJ IDEA Community Edition repo (git clone https://github.com/JetBrains/intellij-community) anywhere on the computer (it takes some time), open Sources tab of the Plugin SDK and add path to the cloned repo root. After some scanning IDE will find all src folders - accept them. To have IntelliJ IDEA Community sources matching IntelliJ IDEA Ultimate installation it is recommended to update Community repo (git pull) at the day when Ultimate is released and switch Community repo to the branch that corresponds to the Ultimate installation.
  3.5. Open Plugin SDK Annotations tab and add [IDEA Installation]/lib/jdkAnnotations.jar
4. Clone IntelliJ IDEA plugins source code (https://github.com/JetBrains/intellij-plugins) and open the project in the 'Dart' folder. It is ready to use. Two run configurations are already configured: 'All tests' (run it each time before commit) and 'Dart plugin' that launches IntelliJ IDEA with the Dart plugin built from sources. Important note: to have all tests passing successfully uncomment <depends>HtmlTools</depends> and <depends>com.intellij.css</depends> tags in the plugin.xml file. Do not commit plugin.xml with uncommented <depends/> tags!
