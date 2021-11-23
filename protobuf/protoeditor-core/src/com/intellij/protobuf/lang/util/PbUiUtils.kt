package com.intellij.protobuf.lang.util

import com.intellij.icons.AllIcons
import com.intellij.ide.util.DefaultPsiElementCellRenderer
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.protobuf.ide.settings.PbProjectSettings
import com.intellij.protobuf.lang.PbLangBundle
import com.intellij.psi.PsiDocumentManager
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.ListCellRenderer

internal object PbUiUtils {
  fun selectItemAndApply(variants: List<PbProjectSettings.ImportPathEntry>,
                         editor: Editor,
                         project: Project,
                         selectAction: (PbProjectSettings.ImportPathEntry) -> Unit) {
    PsiDocumentManager.getInstance(project).commitAllDocuments()

    if (variants.isEmpty()) return
    if (variants.size == 1) {
      selectAction(variants.single())
      return
    }

    JBPopupFactory.getInstance().createListPopup(project, createPopup(variants, project, selectAction)) {
      val psiRenderer = DefaultPsiElementCellRenderer()

      ListCellRenderer<PbProjectSettings.ImportPathEntry> { list, value, index, isSelected, cellHasFocus ->
        JPanel(BorderLayout()).apply {
          add(psiRenderer.getListCellRendererComponent(list, value.location, index, isSelected, cellHasFocus))
        }
      }
    }.showInBestPositionFor(editor)
  }

  private fun createPopup(variants: List<PbProjectSettings.ImportPathEntry>,
                          project: Project,
                          selectAction: (PbProjectSettings.ImportPathEntry) -> Unit): BaseListPopupStep<PbProjectSettings.ImportPathEntry> {
    return object : BaseListPopupStep<PbProjectSettings.ImportPathEntry>(PbLangBundle.message("intention.add.import.path.popup.title"), variants) {
      override fun onChosen(selectedValue: PbProjectSettings.ImportPathEntry?, finalChoice: Boolean): PopupStep<String>? {
        if (selectedValue == null || project.isDisposed) return null

        if (finalChoice) {
          selectAction(selectedValue)
        }
        return null
      }

      override fun isSpeedSearchEnabled() = true
      override fun isAutoSelectionEnabled() = false
      override fun hasSubstep(selectedValue: PbProjectSettings.ImportPathEntry?) = false
      override fun getTextFor(value: PbProjectSettings.ImportPathEntry) = value.location
      override fun getIconFor(value: PbProjectSettings.ImportPathEntry) = AllIcons.ObjectBrowser.AbbreviatePackageNames
    }
  }
}

