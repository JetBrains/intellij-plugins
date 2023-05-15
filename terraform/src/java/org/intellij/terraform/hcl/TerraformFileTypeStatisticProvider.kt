// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl

import com.intellij.internal.statistic.fileTypes.FileTypeStatisticProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.hil.HILFileType

internal class TerraformFileTypeStatisticProvider : FileTypeStatisticProvider {
  override fun getPluginId(): String = "org.intellij.plugins.hcl"

  override fun accept(editor: Editor, fileType: FileType): Boolean {
    return fileType === HCLFileType ||
           fileType === TerraformFileType ||
           fileType === HILFileType
  }
}
