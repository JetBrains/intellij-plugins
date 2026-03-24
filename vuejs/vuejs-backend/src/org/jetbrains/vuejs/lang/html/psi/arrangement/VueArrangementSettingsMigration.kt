// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.psi.arrangement

import com.intellij.application.options.codeStyle.CodeStyleSchemesModel
import com.intellij.ide.util.PropertiesComponent
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.arrangement.ArrangementSettings
import com.intellij.xml.arrangement.HtmlRearranger
import org.jetbrains.vuejs.lang.html.VueLanguage

private class VueArrangementSettingsMigration : ProjectActivity {
  companion object {
    const val VUE_REARRANGER_SETTINGS_MIGRATION = "vue.rearranger.settings.migration"
  }

  override suspend fun execute(project: Project) {
    val propertiesComponent = PropertiesComponent.getInstance(project)
    if (propertiesComponent.isTrueValue(VUE_REARRANGER_SETTINGS_MIGRATION)) {
      return
    }

    val codeStyleSchemesModel = CodeStyleSchemesModel(project)
    val map = mutableMapOf <CommonCodeStyleSettings, ArrangementSettings>()
    codeStyleSchemesModel.schemes.asSequence()
      .map { it.codeStyleSettings }
      .forEach { codeStyleSettings ->
        codeStyleSettings
          .getCommonSettings(HTMLLanguage.INSTANCE)
          .takeIf { it != HtmlRearranger().defaultSettings }
          ?.arrangementSettings
          ?.let {
            val vueSettings = codeStyleSettings.getCommonSettings(VueLanguage)
            if (vueSettings.arrangementSettings == null) {
              map.put(vueSettings, it)
            }
          }
      }
    if (!map.isEmpty()) {
      edtWriteAction {
        if (!propertiesComponent.isTrueValue(VUE_REARRANGER_SETTINGS_MIGRATION)) {
          map.forEach { (vueSettings, arrangementSettings) ->
            vueSettings.setArrangementSettings(arrangementSettings)
          }
          codeStyleSchemesModel.apply()
        }
      }
    }
    propertiesComponent.setValue(VUE_REARRANGER_SETTINGS_MIGRATION, true)
  }
}