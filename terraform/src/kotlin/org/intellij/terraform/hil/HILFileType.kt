// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil

import com.intellij.openapi.fileTypes.LanguageFileType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.Icons
import javax.swing.Icon

object HILFileType : LanguageFileType(HILLanguage) {
  private const val DEFAULT_EXTENSION: String = "hil"

  override fun getIcon(): Icon {
    return Icons.FileTypes.HIL
  }

  override fun getDefaultExtension(): String {
    return DEFAULT_EXTENSION
  }

  override fun getDescription(): String {
    return HCLBundle.message("HILFileType.description")
  }

  override fun getName(): String {
    return "HIL"
  }

  override fun toString() = name
}
