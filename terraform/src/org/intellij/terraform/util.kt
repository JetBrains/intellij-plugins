// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.opentofu.OpenTofuFileType

fun String.nullize(nullizeSpaces: Boolean = false): String? {
  return StringUtil.nullize(this, nullizeSpaces)
}

internal fun isTerraformFileExtension(extension: String?): Boolean {
  return extension == TerraformFileType.DEFAULT_EXTENSION || extension == TerraformFileType.TFVARS_EXTENSION
}

fun isTerraformPsiFile(file: PsiFile): Boolean {
  return file.fileType is TerraformFileType || file.fileType is OpenTofuFileType
}

fun joinCommaOr(list: List<String>): String = when (list.size) {
  0 -> ""
  1 -> list.first()
  else -> (list.dropLast(1).joinToString(postfix = " or " + list.last()))
}

