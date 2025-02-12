// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.editor

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.file.exclude.OverrideFileTypeManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.template.TerraformTemplateFileType
import org.intellij.terraform.template.knownTemplateExtensions

internal class TfRemoveFileTypeAssociationIntention : IntentionAction {

  override fun startInWriteAction(): Boolean {
    return false
  }

  override fun getFamilyName(): String {
    return HCLBundle.message("inspection.possible.template.name")
  }

  override fun getText(): String {
    return HCLBundle.message("inspection.possible.template.remove.association.fix.name")
  }

  override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
    return file != null && isFileWithAlreadyOverriddenTemplateType(file.virtualFile)
  }

  override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
    if (file == null) return
    OverrideFileTypeManager.getInstance().removeFile(file.virtualFile)
    TemplateDataLanguageMappings.getInstance(project).setMapping(file.virtualFile, null)
  }
}

internal fun isFileWithAlreadyOverriddenTemplateType(file: VirtualFile): Boolean {
  return FileTypeManager.getInstance().getFileTypeByFile(file) == TerraformTemplateFileType
         && file.extension !in knownTemplateExtensions
}
