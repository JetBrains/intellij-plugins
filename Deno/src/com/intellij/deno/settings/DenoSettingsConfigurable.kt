package com.intellij.deno.settings

import com.intellij.deno.DenoBundle
import com.intellij.deno.DenoSettings
import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.LanguageTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.MutableProperty
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.PathUtil

internal class DenoSettingsConfigurable(
  private val project: Project,
) : BoundSearchableConfigurable(
  displayName = DenoBundle.message("deno.name"),
  helpTopic = "reference.settings.javascript.deno",
  _id = "settings.javascript.deno"
) {

  override fun createPanel(): DialogPanel {
    return panel {
      val service = DenoSettings.getService(project)
      row(DenoBundle.message("deno.path")) {
        val denoPathDescriptor = FileChooserDescriptorFactory.singleFile().withTitle(DenoBundle.message("deno.name"))
        textFieldWithBrowseButton(denoPathDescriptor)
          .align(AlignX.FILL)
          .bindText(MutableProperty(
            getter = { service.getDenoPath() },
            setter = { service.setDenoPath(it) }
          ))
      }

      row(DenoBundle.message("deno.cache")) {
        val denoCacheDescriptor = FileChooserDescriptorFactory.singleFile().withTitle(DenoBundle.message("deno.cache.title"))
        textFieldWithBrowseButton(denoCacheDescriptor)
          .align(AlignX.FILL)
          .bindText(MutableProperty(
            getter = { PathUtil.toSystemDependentName(service.getDenoCache()) },
            setter = { service.setDenoCache(PathUtil.toSystemIndependentName(it)) }
          ))
      }

      row {
        label(DenoBundle.message("deno.cache.init"))
          .align(AlignY.TOP)
        val editorTextField = createInitCommandEditorTextField()
        cell(editorTextField)
          .align(AlignX.FILL)
          .bind(
            componentGet = { editorTextField.text.trim() },
            componentSet = { _, value ->
              editorTextField.text = value
            },
            prop = MutableProperty(
              getter = { service.getDenoInit() },
              setter = { service.setDenoInit(it) }
            )
          )
      }.layout(RowLayout.PARENT_GRID)

      row {
        checkBox(DenoBundle.message("deno.formatting.enable"))
          .bindSelected(MutableProperty(
            getter = { service.isDenoFormattingEnabled() },
            setter = { service.setDenoFormattingEnabled(it) }
          ))
      }
    }
  }

  private fun createInitCommandEditorTextField(): LanguageTextField {
    val jsonTextField = LanguageTextField(JsonLanguage.INSTANCE, project, "", false)
    jsonTextField.setFontInheritedFromLAF(false)
    jsonTextField.addSettingsProvider { editor ->
      editor.getSettings().additionalLinesCount = 0
    }
    return jsonTextField
  }
}
