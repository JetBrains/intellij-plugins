How to setup IntelliJ IDEA project to work with the Flex plugin source code.

Prerequisites:
- Oracle JDK 1.8
- Git command line client
- The latest IntelliJ IDEA Ultimate installed (usually the latest is the EAP: http://eap.jetbrains.com/idea/). Keep the installation up-to-date.
  The following bundled plugins must be enabled: Git Integration, I18n for Java, IntelliLang, JUnit, Plugin DevKit, Properties Support, UI Designer.

1. Clone the following 2 repositories to neighbor folders:
   - git clone https://github.com/JetBrains/intellij-plugins,
   - git clone https://github.com/JetBrains/intellij-community

2. Configure JDK and Plugin SDK.

  2.1. Start IntelliJ IDEA, on Welcome screen click Configure | Project Defaults | Project Structure | SDKs, click [+] and add JDK 1.8.
       Make sure tools.jar is listed in the JDK Classpath tab or add it manually ([JDK home]/lib/tools.jar)

  2.2. In the same dialog (Project Structure | SDKs) click [+] again to add one more SDK, select 'IntelliJ Platform Plugin SDK' and provide path to the current IntelliJ IDEA installation. Agree to use JDK 1.8 as an internal Java platform for this Plugin SDK.

  2.3. Rename just added Plugin SDK to 'IntelliJ IDEA Ultimate'.

  2.4. Add some more jars to the Plugin SDK Classpath:
         [IDEA Installation]/plugins/ant/lib/*.jar
         [IDEA Installation]/plugins/copyright/lib/*.jar
         [IDEA Installation]/plugins/CSS/lib/*.jar
         [IDEA Installation]/plugins/JavaScriptLanguage/lib/*.jar
         [IDEA Installation]/plugins/maven/lib/*.jar
         [IDEA Installation]/plugins/properties/lib/*.jar
         [IDEA Installation]/plugins/uml/lib/*.jar
         [IDEA Installation]/plugins/w3validators/lib/*.jar

  2.5. Open Sources tab of the Plugin SDK and add path to the cloned intellij-community repository. After some scanning IDE will find all src folders - accept them. To have IntelliJ IDEA Community sources matching IntelliJ IDEA Ultimate installation it is recommended to checkout tag with the name equal to the IntelliJ IDEA Ultimate build number, for example 'git checkout idea/142.4675.3'.

  2.6. Open Plugin SDK Annotations tab and add [IDEA Installation]/lib/jdkAnnotations.jar if its is not already there.

  2.7. OK to close the Project Structure dialog.

3. Open the project intellij-plugins/flex (Welcome screen | Open). Project is ready to use. There are 2 preconfigured run configurations:
   'Flex plugin' - to start IntelliJ IDEA + Flex Plugin from sources,
   'All tests' - to run FlexTestSuite.

4. If you are going to submit a GitHub pull request please add a corresponding test (if possible) and make sure that your contribution doesn't break existing tests.


How to debug compiler.

  Compiler is started as a separate Java process (see com.intellij.compiler.server.BuildManager#launchBuildProcess). In its turn it starts one more Java process for the Flex compiler (see JpsBuiltInFlexCompilerHandler#startCompilerProcess). So none of those is possible to debug by simply running 'Flex plugin' run configuration.
  To debug main compiler process (that runs code from 'flex-plugin-jps' module use 'Compiler debug' remote debug configuration:
  - Open Run | Edit Configurations | 'Flex plugin' and change the value of the -Dcompiler.process.debug.port VM option to the port that is specified in the 'Compiler debug' configuration (5660 by default). To disable compiler debugging set the value back to -1.
  - Start 'Flex plugin' run configuration (run or debug - both are ok), when IDE starts - trigger any compilation. Compiler process will be waiting for the remote debug connection.
  - Start 'Compiler debug' run configuration.

  Note:
  - Code from the 'flex-plugin' module works only in the IDE main java process, so it is debuggable only using the 'Flex plugin' run configuration.
  - Code from the 'flex-plugin-jps' module works only in the compiler java process, so it is debuggable only using the 'Compiler debug' run configuration.
  - Code from the 'flex-plugin-shared' module works both in the IDE main and in the compiler java processes, so breakpoints there may be hit when running any (or both) of these run configurations.

  To debug 'Built-in compiler shell' (shell that manages multi-threaded Flex compilation) you need to setup a project based on the sources from the intellij-plugins/flex/tools/BuiltInFlexCompiler folder, configure Remote Debug configuration and add required VM options to the JpsBuiltInFlexCompilerHandler.startCompilerProcess() method.


Troubleshooting.

1. Q: Some code is red and compilation fails with errors.
   A: Most likely classes with red code were recently updated to match current state of the original IntelliJ IDEA Ultimate repository (which is not fully open source). The code will become green again when the next EAP or official release is out. While waiting for the next release just fix compilation locally (for example by reverting affected files to their previous git revisions).

2. Q: Some tests fail even without any changes in the plugin source code.
   A: Delete folder plugins-sandbox/test and run tests again. Path to the plugins-sandbox folder is shown in Project Structure | SDKs | IntelliJ IDEA Ultimate

3. Q: Some tests fail anyway.
   A: About 20 tests are known to fail and will be fixed soon. Just make sure not to break passing tests and keep IntelliJ IDEA installation up-to-date.
