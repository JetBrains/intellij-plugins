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
        var whiteSpace: Boolean
            get(){
                return PropertiesComponent.getInstance()!!.getBoolean(addWhiteSpaceName, true)
            }
            set(value: Boolean) {
                var str:String = "true"
                if(!value) str = "false"
                PropertiesComponent.getInstance()!!.setValue(addWhiteSpaceName, str)
            }
    }
}