// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.HCL_DEFAULT_EXTENSION
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

object TerragruntFileType : LanguageFileType(HCLLanguage) {
  override fun getName(): @NonNls String = "Terragrunt"

  override fun getDescription(): @NlsContexts.Label String = HCLBundle.message("terragrunt.display.name")

  override fun getDefaultExtension(): @NlsSafe String = HCL_DEFAULT_EXTENSION

  override fun getIcon(): Icon = TerraformIcons.Terragrunt

  override fun getDisplayName(): @Nls String = HCLBundle.message("terragrunt.display.name")
}

internal const val TERRAGRUNT_MAIN_FILE = "terragrunt.hcl"
internal const val TERRAGRUNT_STACK_FILE = "terragrunt.stack.hcl"