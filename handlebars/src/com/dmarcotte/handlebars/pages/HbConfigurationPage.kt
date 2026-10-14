// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.pages

import com.dmarcotte.handlebars.HbBundle
import com.dmarcotte.handlebars.HbLanguage
import com.dmarcotte.handlebars.config.HbConfig
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.EmptyRunnable
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.listCellRenderer.listCellRenderer

open class HbConfigurationPage(private val project: Project) :
  BoundSearchableConfigurable(HbBundle.message("hb.pages.options.title"),
                              "reference.settings.ide.settings.handlebars.mustache",
                              "editor.preferences.handlebarsOptions") {

  override fun createPanel(): DialogPanel {
    // add using the native Handlebars commenter as an option
    val languages = (TemplateDataLanguageMappings.getTemplateableLanguages() + HbLanguage.INSTANCE).sortedBy { it.id }

    return panel {
      row {
        checkBox(HbBundle.message("hb.pages.options.generate.closing.tag"))
          .bindSelected(HbConfig::isAutoGenerateCloseTagEnabled, HbConfig::setAutoGenerateCloseTagEnabled)
      }
      row {
        checkBox(HbBundle.message("hb.pages.options.complete.close.mustache"))
          .bindSelected(HbConfig::isAutocompleteMustachesEnabled, HbConfig::setAutocompleteMustachesEnabled)
          .contextHelp(HbBundle.message("hb.pages.options.complete.close.tooltip"))
      }
      row {
        checkBox(HbBundle.message("hb.pages.options.formatter"))
          .bindSelected(HbConfig::isFormattingEnabled, HbConfig::setFormattingEnabled)
      }
      row {
        checkBox(HbBundle.message("hb.pages.options.html.as.hb"))
          .bindSelected({ HbConfig.shouldOpenHtmlAsHandlebars(project) }, ::setOpenHtmlAsHandlebars)
      }
      row(HbBundle.message("hb.page.options.commenter.language")) {
        comboBox(languages, listCellRenderer("") {
          value.associatedFileType?.icon?.let { icon(it) }
          text(value.displayName)
        })
          .bindItem(HbConfig::getCommenterLanguage, HbConfig::setCommenterLanguage)
          .contextHelp(HbBundle.message("hb.page.options.commenter.language.tooltip"))
      }
    }
  }

  private fun setOpenHtmlAsHandlebars(value: Boolean) {
    if (HbConfig.setShouldOpenHtmlAsHandlebars(value, project)) {
      runWriteAction {
        FileTypeManagerEx.getInstanceEx().makeFileTypesChange("hb config updated", EmptyRunnable.getInstance())
      }
    }
  }
}
