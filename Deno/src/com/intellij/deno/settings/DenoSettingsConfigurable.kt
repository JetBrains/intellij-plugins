package com.intellij.deno.settings

import com.intellij.codeInsight.template.impl.TemplateEditorUtil
import com.intellij.deno.DenoBundle
import com.intellij.deno.DenoSettings
import com.intellij.json.JsonLanguage
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.dsl.builder.*
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
      row(DenoBundle.message("settings.deno.path.javascriptRuntimePage.label")) {
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
        val editor = createInitCommandEditor(disposable!!)
        cell(editor.component)
          .align(AlignX.FILL)
          .bind(
            componentGet = { editor.document.text.trim() },
            componentSet = { _, value ->
              ApplicationManager.getApplication().runWriteAction {
                editor.document.setText(value)
              }
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

  private fun createInitCommandEditor(parentDisposable: Disposable): Editor {
    val fakeFile = PsiFileFactory.getInstance(project).createFileFromText("dummy", JsonLanguage.INSTANCE, "", true, false)
    val document = PsiDocumentManager.getInstance(project).getDocument(fakeFile)
    val editor = TemplateEditorUtil.createEditor(false, document, project)
    editor.settings.additionalLinesCount = 0
    Disposer.register(parentDisposable) {
      EditorFactory.getInstance().releaseEditor(editor)
    }
    return editor
  }
}
