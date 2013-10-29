Steps to configure this 'Dart-plugin' project:

* Open Project Structure
* SDKs -> [+] button -> IntelliJ Platform Plugin SDK -> Choose a folder with IntelliJ IDEA Ultimate installation (*.App on Mac). SDK name should be 'IDEA Ultimate'.
* Go to the SDK's settings page -> Classpath tab -> [+] button at the tab -> add plugins: JavaScriptLanguage, JavaScriptDebug, Copyright, FileWatchers
  ** To add a plugin go to IntelliJ IDEA folder/plugins/<plugin-name>/lib and choose all jars
  ** To add fileWatchers plugin download the plugin from the repo http://plugins.jetbrains.com/plugin?pr=idea&pluginId=7177 and add jars from the zip file