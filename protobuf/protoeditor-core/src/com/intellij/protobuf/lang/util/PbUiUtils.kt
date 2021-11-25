package com.intellij.protobuf.lang.util

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
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer

internal object PbUiUtils {
  fun selectItemAndApply(variants: List<PbImportIntentionVariant>,
                         editor: Editor,
                         project: Project) {
    PsiDocumentManager.getInstance(project).commitAllDocuments()

    if (variants.isEmpty()) return
    if (variants.size == 1) {
      variants.single().invokeAction(project)
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
            append("/", SimpleTextAttributes.GRAYED_ATTRIBUTES)
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
}

