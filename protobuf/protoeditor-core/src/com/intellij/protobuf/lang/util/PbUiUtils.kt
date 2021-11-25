package com.intellij.protobuf.lang.util

import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.protobuf.lang.PbLangBundle
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import java.awt.BorderLayout
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer

internal object PbUiUtils {
  fun selectItemAndApply(variants: List<ImportPathData>,
                         editor: Editor,
                         project: Project,
                         selectAction: (ImportPathData) -> Unit) {
    PsiDocumentManager.getInstance(project).commitAllDocuments()

    if (variants.isEmpty()) return
    if (variants.size == 1) {
      selectAction(variants.single())
      return
    }

    val customRenderer = createRenderer()
    JBPopupFactory.getInstance().createListPopup(project, createPopup(variants, project, selectAction)) {
      ListCellRenderer<ImportPathData> { list, value, index, isSelected, cellHasFocus ->
        JPanel(BorderLayout()).apply {
          add(customRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus))
        }
      }
    }.showInBestPositionFor(editor)
  }

  private fun createPopup(variants: List<ImportPathData>,
                          project: Project,
                          selectAction: (ImportPathData) -> Unit): BaseListPopupStep<ImportPathData> {
    return object : BaseListPopupStep<ImportPathData>(PbLangBundle.message("intention.add.import.path.popup.title"), variants) {

      override fun onChosen(selectedValue: ImportPathData?, finalChoice: Boolean): PopupStep<Any>? {
        if (selectedValue == null || project.isDisposed) return null

        if (finalChoice) {
          selectAction(selectedValue)
        }
        return null
      }

      override fun isSpeedSearchEnabled() = true
      override fun isAutoSelectionEnabled() = false
      override fun hasSubstep(selectedValue: ImportPathData?) = false
      override fun getTextFor(value: ImportPathData) = value.presentablePath
      override fun getIconFor(value: ImportPathData) = AllIcons.Actions.ModuleDirectory
    }
  }

  private fun createRenderer(): ListCellRenderer<ImportPathData> {
    return object : ColoredListCellRenderer<ImportPathData>() {

      override fun customizeCellRenderer(list: JList<out ImportPathData>,
                                         value: ImportPathData,
                                         index: Int,
                                         selected: Boolean,
                                         hasFocus: Boolean) {
        append(value.presentablePath, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        append("/", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        append(value.originalImportStatement, SimpleTextAttributes.GRAYED_ATTRIBUTES)
        icon = AllIcons.Actions.ModuleDirectory
      }
    }
  }
}

