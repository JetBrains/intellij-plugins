// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.options

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.IndentOptionsEditor
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.psi.formatter.VueCodeStyleSettings

class VueCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {

  override fun getLanguage(): Language = VueLanguage.INSTANCE

  override fun getCodeSample(settingsType: SettingsType): String? = """
      <template>
        <div id="app">
              <img      alt="Vue logo"        
     src="./assets/logo.png">
        <HelloWorld  
     msg =  "Welcome to Your Vue.js App"/></div>
    </template>
    
     <script>
        import HelloWorld  from './components/HelloWorld.vue'
    
        export  default  {
      name:    'App'  ,
         components:     {
        HelloWorld}
      }
    </script>
    
      <style>
           #app      {
      font-family: Avenir, Helvetica, Arial, sans-serif;
       text-align: center;   color    : #2c3e50;}
    </style>
  """.trimIndent()

  override fun getIndentOptionsEditor(): IndentOptionsEditor? {
    return VueIndentOptionsEditor()
  }

  override fun createConfigurable(baseSettings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
    return object : CodeStyleAbstractConfigurable(baseSettings, modelSettings, configurableDisplayName) {
      override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
        return VueCodeStyleMainPanel(currentSettings, settings)
      }

      override fun getHelpTopic(): String? {
        return "reference.settingsdialog.IDE.vuecodestyle"
      }
    }
  }

  override fun createCustomSettings(settings: CodeStyleSettings): CustomCodeStyleSettings? {
    return VueCodeStyleSettings(settings)
  }
}