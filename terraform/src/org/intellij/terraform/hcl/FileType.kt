// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object HCLFileType : LanguageFileType(HCLLanguage) {
  override fun getIcon(): Icon = Icons.FileTypes.HCL

  override fun getDefaultExtension(): String = HCL_DEFAULT_EXTENSION

  override fun getDescription(): String = HCLBundle.message("HCLFileType.description")

  override fun getName(): String = "HCL"
}

internal const val HCL_DEFAULT_EXTENSION = "hcl"