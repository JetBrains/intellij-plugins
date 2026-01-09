// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.intellij.terraform.config.TFVARS_EXTENSION
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.opentofu.OpenTofuFileType
import org.intellij.terraform.stack.component.isTfComponentPsiFile
import org.intellij.terraform.stack.deployment.isTfDeployPsiFile
import org.intellij.terraform.terragrunt.isTerragruntPsiFile

internal fun isTerraformFileExtension(extension: String?): Boolean {
  return extension == TerraformFileType.defaultExtension || extension == TFVARS_EXTENSION
}

internal fun isTfOrTofuExtension(extension: String?): Boolean {
  return isTerraformFileExtension(extension) || extension == OpenTofuFileType.defaultExtension
}

internal fun isTfOrTofuPsiFile(file: PsiFile?): Boolean {
  return isTerraformFile(file) || isTofuFile(file)
}

internal fun isHclCompatiblePsiFile(file: PsiFile?): Boolean {
  return isTfOrTofuPsiFile(file) ||
         isTfComponentPsiFile(file) ||
         isTfDeployPsiFile(file) ||
         isTerragruntPsiFile(file)
}

internal fun isTerraformFile(psiFile: PsiFile?): Boolean {
  return psiFile?.fileType is TerraformFileType
}

internal fun isTofuFile(psiFile: PsiFile?): Boolean {
  return psiFile?.fileType is OpenTofuFileType
}

internal fun joinCommaOr(list: List<String>): String = when (list.size) {
  0 -> ""
  1 -> list.first()
  else -> (list.dropLast(1).joinToString(postfix = " or " + list.last()))
}

fun hasHCLLanguageFiles(project: Project, fileTypes: Iterable<FileType>): Boolean {
  return fileTypes
    .asSequence()
    .filter { type -> (type as? LanguageFileType)?.language?.isKindOf(HCLLanguage) == true }
    .any { ft -> FileTypeIndex.containsFileOfType(ft, GlobalSearchScope.allScope(project)) }
}
