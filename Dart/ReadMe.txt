How to start working with this 'Dart-plugin' project.

Prerequisites:
- Oracle JDK 1.6 or 1.7
- git command line client

1. Install the latest IntelliJ IDEA Ultimate Edition. Usually it means Early Access build: http://eap.jetbrains.com/idea. As of 03.03.2014 it is IntelliJ IDEA 13.1 EAP (build 134.1342). Keep IDE installation up-to-date.
2. Install 'Grammar-Kit' and 'File Watchers' plugins: launch IntelliJ IDEA, on Welcome screen click Configure | Plugins. Press 'Install JetBrains plugin...' button to find 'File Watchers' plugin and then 'Browse repositories...' to find 'Grammar-Kit'. Restart IDE.
3. Configure JDK and Plugin SDK.
  3.1. Launch IntelliJ IDEA, on Welcome screen click Configure | Project Defaults | Project Structure | SDKs, click [+] and add JDK (1.6 or 1.7), then click [+] again, select 'IntelliJ Platform Plugin SDK' and provide path to the current IntelliJ IDEA installation.
  3.2. Rename Plugin SDK to 'IDEA Ultimate'.
  3.3. Add some jars to the Plugin SDK Classpath:
         [IDEA Installation]/plugins/copyright/lib/*.jar
         [IDEA Installation]/plugins/JavaScriptDebugger/lib/*.jar
         [IDEA config folder]/plugins/fileWatcher/lib/*.jar (see here how to locate [IDEA config folder]: https://intellij-support.jetbrains.com/entries/23358108)
  3.4. Clone IntelliJ IDEA Community Edition repo (git clone https://github.com/JetBrains/intellij-community) anywhere on the computer (it takes some time), open Sources tab of the Plugin SDK and add path to the cloned repo root. After some scanning IDE will find all src folders - accept them. Update IntelliJ IDEA Community sources from time to time, e.g. when you upgrade IntelliJ IDEA Ultimate installation (git pull).
  3.5. Open Plugin SDK Annotations tab and add [IDEA Installation]/lib/jdkAnnotations.jar
4. Clone IntelliJ IDEA plugins source code (https://github.com/JetBrains/intellij-plugins) and open the project in the 'Dart' folder. It is ready to use. Two run configurations are already configured: 'Dart tests' (run it each time before commit) and 'Dart plugin' that launches IntelliJ IDEA with Dart plugin from sources.
