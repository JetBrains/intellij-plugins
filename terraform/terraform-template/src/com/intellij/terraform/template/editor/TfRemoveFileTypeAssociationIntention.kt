// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.terraform.template.editor

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.file.exclude.OverrideFileTypeManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import com.intellij.terraform.template.TftplBundle
import com.intellij.terraform.template.TftplFileType
import com.intellij.terraform.template.knownTemplateExtensions

internal class TfRemoveFileTypeAssociationIntention : IntentionAction {

  override fun startInWriteAction(): Boolean {
    return false
  }

  override fun getFamilyName(): String {
    return TftplBundle.message("inspection.possible.template.name")
  }

  override fun getText(): String {
    return TftplBundle.message("inspection.possible.template.remove.association.fix.name")
  }

  override fun isAvailable(project: Project, editor: Editor?, psiFile: PsiFile?): Boolean {
    return psiFile != null && isFileWithAlreadyOverriddenTemplateType(psiFile.virtualFile)
  }

  override fun invoke(project: Project, editor: Editor?, psiFile: PsiFile?) {
    if (psiFile == null) return
    OverrideFileTypeManager.getInstance().removeFile(psiFile.virtualFile)
    TemplateDataLanguageMappings.getInstance(project).setMapping(psiFile.virtualFile, null)
  }
}

internal fun isFileWithAlreadyOverriddenTemplateType(file: VirtualFile): Boolean {
  return FileTypeManager.getInstance().getFileTypeByFile(file) == TftplFileType
         && file.extension !in knownTemplateExtensions
}
