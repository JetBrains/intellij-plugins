// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform

import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.opentofu.OpenTofuFileType

internal fun isTerraformFileExtension(extension: String?): Boolean {
  return extension == TerraformFileType.DEFAULT_EXTENSION || extension == TerraformFileType.TFVARS_EXTENSION
}

internal fun isTerraformCompatibleExtension(extension: String?): Boolean {
  return isTerraformFileExtension(extension) || extension == OpenTofuFileType.DEFAULT_EXTENSION
}

internal fun isTerraformCompatiblePsiFile(file: PsiFile?): Boolean {
  return isTerraformFile(file) || isOpenTofuFile(file)
}

internal fun isTerraformFile(psiFile: PsiFile?): Boolean {
  return psiFile?.fileType is TerraformFileType
}

internal fun isOpenTofuFile(psiFile: PsiFile?): Boolean {
  return psiFile?.fileType is OpenTofuFileType
}

internal fun joinCommaOr(list: List<String>): String = when (list.size) {
  0 -> ""
  1 -> list.first()
  else -> (list.dropLast(1).joinToString(postfix = " or " + list.last()))
}

suspend fun hasHCLLanguageFiles(project: Project, fileType: LanguageFileType): Boolean {
  return smartReadAction(project) { hasHCLLanguageFiles(project, listOf(fileType)) }
}

fun hasHCLLanguageFiles(project: Project, fileTypes: Iterable<FileType>):Boolean {
  return fileTypes
    .asSequence()
    .filter { type -> (type as? LanguageFileType)?.language?.isKindOf(HCLLanguage) == true }
    .any { ft -> FileTypeIndex.containsFileOfType(ft, GlobalSearchScope.allScope(project)) }
}
