// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.options

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.lang.Language
import com.intellij.lang.javascript.JavaScriptBundle
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.*
import org.angular2.lang.html.Angular17HtmlLanguage
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.psi.formatter.Angular2HtmlCodeStyleSettings

class Angular2HtmlCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {

  override fun getLanguage(): Language = Angular2HtmlLanguage.INSTANCE

  override fun getCodeSample(settingsType: SettingsType): String = """
    <human-profile     *ngIf="user.isHuman else robot" 
    [data]="user"/>
    <ng-template #robot>
                <p     *ngIf="user.isRobot"> {{ user . name    }}
    </ng-template>

    @if      (    user.isHuman    )       {
          <human-profile   [data]="user"/>
    } 
    
    @else if 
    (   user.isRobot  ) 
    {
      {{user.    name    
       }}
    }  @else      {
          <p>The profile is unknown!</p>
    }
  """.trimIndent()

  override fun createFileFromText(project: Project, text: String): PsiFile? =
    PsiFileFactory.getInstance(project).createFileFromText(
      "angular.html", Angular17HtmlLanguage.INSTANCE, text, false, true)

  override fun createConfigurable(baseSettings: CodeStyleSettings, modelSettings: CodeStyleSettings): CodeStyleConfigurable {
    return object : CodeStyleAbstractConfigurable(baseSettings, modelSettings, configurableDisplayName) {
      override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
        return Angular2CodeStyleMainPanel(currentSettings, settings)
      }

      override fun getHelpTopic(): String {
        return "reference.settingsdialog.IDE.angular2codestyle"
      }
    }
  }

  override fun customizeDefaults(commonSettings: CommonCodeStyleSettings, indentOptions: CommonCodeStyleSettings.IndentOptions) {
  }

  override fun useBaseLanguageCommonSettings(): Boolean {
    return true
  }

  override fun createCustomSettings(settings: CodeStyleSettings): CustomCodeStyleSettings {
    return Angular2HtmlCodeStyleSettings(settings)
  }

  override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
    when (settingsType) {
      SettingsType.SPACING_SETTINGS -> {
        consumer.showCustomOption(Angular2HtmlCodeStyleSettings::class.java, "SPACES_WITHIN_INTERPOLATION_EXPRESSIONS",
                                  JavaScriptBundle.message("javascript.formatting.web.spacing.within.interpolations"),
                                  JavaScriptBundle.message("javascript.formatting.web.spacing.within.group"))
      }
      SettingsType.WRAPPING_AND_BRACES_SETTINGS -> {

        consumer.showCustomOption(Angular2HtmlCodeStyleSettings::class.java,
                                  "INTERPOLATION_WRAP",
                                  JavaScriptBundle.message("javascript.formatting.web.wrapping.interpolations"),
                                  null,
                                  CodeStyleSettingsCustomizableOptions.getInstance().WRAP_OPTIONS,
                                  CodeStyleSettingsCustomizable.WRAP_VALUES)
        consumer.showCustomOption(Angular2HtmlCodeStyleSettings::class.java,
                                  "INTERPOLATION_NEW_LINE_AFTER_START_DELIMITER",
                                  JavaScriptBundle.message("javascript.formatting.web.wrapping.new-line-after-start-delimiter"),
                                  JavaScriptBundle.message("javascript.formatting.web.wrapping.interpolations"))
        consumer.showCustomOption(Angular2HtmlCodeStyleSettings::class.java,
                                  "INTERPOLATION_NEW_LINE_BEFORE_END_DELIMITER",
                                  JavaScriptBundle.message("javascript.formatting.web.wrapping.new-line-before-end-delimiter"),
                                  JavaScriptBundle.message("javascript.formatting.web.wrapping.interpolations"))

      }
      else -> {
      }
    }
  }
}