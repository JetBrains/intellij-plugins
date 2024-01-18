package com.intellij.dts.ide

import com.intellij.dts.DtsBundle
import com.intellij.dts.DtsIcons
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

class DtsNewFileAction : CreateFileFromTemplateAction(), DumbAware {
  companion object {
    private const val SOURCE_FILE_TEMPLATE = "devicetree_source"
    private const val INCLUDE_FILE_TEMPLATE = "devicetree_include"
    private const val OVERLAY_FILE_TEMPLATE = "devicetree_overlay"
  }

  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder.setTitle(DtsBundle.message("action.dts_new_file.title"))
      .addKind(DtsBundle.message("action.dts_new_file.kind.source"), DtsIcons.Dts, SOURCE_FILE_TEMPLATE)
      .addKind(DtsBundle.message("action.dts_new_file.kind.include"), DtsIcons.Dts, INCLUDE_FILE_TEMPLATE)
      .addKind(DtsBundle.message("action.dts_new_file.kind.overlay"), DtsIcons.Dts, OVERLAY_FILE_TEMPLATE)
  }

  override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
    return DtsBundle.message("action.dts_new_file.name", newName)
  }
}