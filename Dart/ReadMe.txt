Steps to configure a plugin SDK:

* Open Module Settings
* SDKs -> + button -> IntelliJ Platform Plugin SDK -> Choose a folder with IntelliJ Ultimate(!) or *.App on Mac
* Go to the SDK's settings page -> Classpath tab -> + button(bottom left corner) -> add plugins: JavaScriptLanguage, copyright, yaml, fileWatchers
* To add a plugin go to IntelliJ IDEA folder/plugins/<plugin-name>/lib and choose all jars
* To add fileWatchers plugin just download the plugin from our repo http://plugins.jetbrains.com/plugin?pr=idea&pluginId=7177 and add jars from the zip file