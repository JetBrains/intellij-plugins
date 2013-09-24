package org.angularjs

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ide.util.PropertiesComponent

/**
 * Created by johnlindquist on 7/10/13.
 */
public abstract class AngularJSConfig() {
    class object {
        var componentName: String = "AngularJSConfig"
        val addWhiteSpaceName: String = "AngularJSConfig.addWhitespaceBetweenBraces"
        val braceEnabledName: String = "AngularJSConfig.braceEnabled"
        var whiteSpace: Boolean
            get(){
                return PropertiesComponent.getInstance()!!.getBoolean(addWhiteSpaceName, false)
            }
            set(value: Boolean) {
                var str:String = "true"
                if(!value) str = "false"
                PropertiesComponent.getInstance()!!.setValue(addWhiteSpaceName, str)
            }
        var braceEnabled: Boolean
            get(){
                return PropertiesComponent.getInstance()!!.getBoolean(braceEnabledName, false)
            }
            set(value: Boolean) {
                var str:String = "true"
                if(!value) str = "false"
                PropertiesComponent.getInstance()!!.setValue(braceEnabledName, str)
            }
    }
}