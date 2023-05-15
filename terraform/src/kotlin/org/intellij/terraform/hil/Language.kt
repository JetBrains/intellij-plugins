// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType


object HILLanguage : Language("HIL") {
  override fun isCaseSensitive() = true
  override fun getAssociatedFileType(): LanguageFileType {
    return HILFileType
  }

  override fun getDisplayName(): String {
    return "HashiCorp Interpolation Language"
  }
}

