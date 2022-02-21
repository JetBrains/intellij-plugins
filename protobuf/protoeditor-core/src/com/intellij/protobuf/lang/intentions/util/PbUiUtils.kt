package com.intellij.protobuf.lang.intentions.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.protobuf.lang.PbLangBundle
import com.intellij.protobuf.lang.intentions.PbImportIntentionVariant
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import java.awt.BorderLayout
import java.io.File
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer

internal fun selectItemAndApply(variants: List<PbImportIntentionVariant>,
                                editor: Editor,
                                project: Project) {
  PsiDocumentManager.getInstance(project).commitAllDocuments()

  if (variants.isEmpty()) return
  if (instantlyInvokeSingleIntention(variants, project)) return
  if (ApplicationManager.getApplication().isUnitTestMode) {
    handleUnitTestMode(variants, project)
    return
  }

  val customRenderer = createRenderer()
  JBPopupFactory.getInstance().createListPopup(project, createPopup(variants, project)) {
    ListCellRenderer<PbImportIntentionVariant> { list, value, index, isSelected, cellHasFocus ->
      JPanel(BorderLayout()).apply {
        add(customRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus))
      }
    }
  }.showInBestPositionFor(editor)
}

private fun instantlyInvokeSingleIntention(variants: List<PbImportIntentionVariant>, project: Project): Boolean {
  val theOnlyAvailableIntention = variants.filterIsInstance<PbImportIntentionVariant.AddImportPathToSettings>().singleOrNull()
  return theOnlyAvailableIntention.invokeIntentionIfNotNull(project)
}

private fun handleUnitTestMode(variants: List<PbImportIntentionVariant>, project: Project): Boolean {
  val firstAvailableSortedIntention =
    variants.filterIsInstance<PbImportIntentionVariant.AddImportPathToSettings>()
      .minByOrNull { it.importPathData.presentablePath }
  return firstAvailableSortedIntention.invokeIntentionIfNotNull(project)
}

private fun PbImportIntentionVariant.AddImportPathToSettings?.invokeIntentionIfNotNull(project: Project): Boolean {
  this?.invokeAction(project) ?: return false
  return true
}

private fun createPopup(variants: List<PbImportIntentionVariant>,
                        project: Project): BaseListPopupStep<PbImportIntentionVariant> {
  return object : BaseListPopupStep<PbImportIntentionVariant>(PbLangBundle.message("intention.add.import.path.popup.title"),
                                                              variants) {

    override fun onChosen(selectedValue: PbImportIntentionVariant?, finalChoice: Boolean): PopupStep<Any>? {
      if (selectedValue == null || project.isDisposed) return null

      if (finalChoice) {
        selectedValue.invokeAction(project)
      }
      return null
    }

    override fun isSpeedSearchEnabled() = true
    override fun isAutoSelectionEnabled() = false
  }
}

private fun createRenderer(): ListCellRenderer<PbImportIntentionVariant> {
  return object : ColoredListCellRenderer<PbImportIntentionVariant>() {

    override fun customizeCellRenderer(list: JList<out PbImportIntentionVariant>,
                                       value: PbImportIntentionVariant,
                                       index: Int,
                                       selected: Boolean,
                                       hasFocus: Boolean) {
      when (value) {
        is PbImportIntentionVariant.AddImportPathToSettings -> {
          append(value.importPathData.presentablePath, SimpleTextAttributes.REGULAR_ATTRIBUTES)
          @Suppress("HardCodedStringLiteral")
          append(File.separator, SimpleTextAttributes.GRAYED_ATTRIBUTES)
          append(value.importPathData.originalImportStatement, SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }
        is PbImportIntentionVariant.ManuallyConfigureImportPathsSettings -> {
          append(value.presentableName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        }
      }
      icon = value.icon
    }
  }
}

