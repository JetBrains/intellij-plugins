// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.options

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.IndentOptionsEditor
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.*
import com.intellij.psi.impl.source.PsiFileImpl
import org.jetbrains.vuejs.VueBundle
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
     {{simple   }}
     {{veryLongLongLongLongLongLongLongLongSimpleInterpolation}}
{{
simpleOverMultipleLines}}
     {{a + complex
     * interpolation + over (multiple - lines)}}
     {{ a + complex *interpolation(in_a-single  - line)}}
     {{12+13}} 
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

  override fun createFileFromText(project: Project, text: String): PsiFile? {
    val first = PsiFileFactory.getInstance(project).createFileFromText(
      "a.vue", VueLanguage.INSTANCE, text, false, true
    )
    // Trick injection manager into providing injections - if original file is present
    // the manager will allow for injections
    val result = PsiFileFactory.getInstance(project).createFileFromText(text, first)
    (result as PsiFileImpl).originalFile = first
    return result
  }

  override fun useTextReformat(): Boolean {
    return true
  }

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

  override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
    when (settingsType) {
      SettingsType.SPACING_SETTINGS -> {
        consumer.showCustomOption(VueCodeStyleSettings::class.java, "SPACES_WITHIN_SIMPLE_INTERPOLATION_EXPRESSIONS",
                                  VueBundle.message("vue.formatting.spacing.within.simple-interpolations"),
                                  VueBundle.message("vue.formatting.spacing.within.group"))
        consumer.showCustomOption(VueCodeStyleSettings::class.java, "SPACES_WITHIN_COMPLEX_INTERPOLATION_EXPRESSIONS",
                                  VueBundle.message("vue.formatting.spacing.within.complex-interpolations"),
                                  VueBundle.message("vue.formatting.spacing.within.group"))
      }
      SettingsType.WRAPPING_AND_BRACES_SETTINGS -> {

        consumer.showCustomOption(VueCodeStyleSettings::class.java,
                                  "SIMPLE_INTERPOLATION_WRAP",
                                  VueBundle.message("vue.formatting.wrapping.simple-interpolations"),
                                  null,
                                  CodeStyleSettingsCustomizable.WRAP_OPTIONS, CodeStyleSettingsCustomizable.WRAP_VALUES)
        consumer.showCustomOption(VueCodeStyleSettings::class.java,
                                  "SIMPLE_INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER",
                                  VueBundle.message("vue.formatting.wrapping.new-line-after-start-delimiter"),
                                  VueBundle.message("vue.formatting.wrapping.simple-interpolations"))
        consumer.showCustomOption(VueCodeStyleSettings::class.java,
                                  "SIMPLE_INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER",
                                  VueBundle.message("vue.formatting.wrapping.new-line-before-end-delimiter"),
                                  VueBundle.message("vue.formatting.wrapping.simple-interpolations"))

        consumer.showCustomOption(VueCodeStyleSettings::class.java,
                                  "COMPLEX_INTERPOLATION_WRAP",
                                  VueBundle.message("vue.formatting.wrapping.complex-interpolations"),
                                  null,
                                  CodeStyleSettingsCustomizable.WRAP_OPTIONS, CodeStyleSettingsCustomizable.WRAP_VALUES)
        consumer.showCustomOption(VueCodeStyleSettings::class.java,
                                  "COMPLEX_INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER",
                                  VueBundle.message("vue.formatting.wrapping.new-line-after-start-delimiter"),
                                  VueBundle.message("vue.formatting.wrapping.complex-interpolations"))
        consumer.showCustomOption(VueCodeStyleSettings::class.java,
                                  "COMPLEX_INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER",
                                  VueBundle.message("vue.formatting.wrapping.new-line-before-end-delimiter"),
                                  VueBundle.message("vue.formatting.wrapping.complex-interpolations"))

      }
      else -> {
      }
    }
  }
}