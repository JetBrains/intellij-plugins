// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.deployment

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

internal object TfDeployFileType : LanguageFileType(HCLLanguage) {

  override fun getName(): @NonNls String = "Terraform Deployment"

  override fun getDescription(): @NlsContexts.Label String = HCLBundle.message("terraform.deploy.name")

  override fun getDefaultExtension(): @NlsSafe String = HCL_DEFAULT_EXTENSION

  override fun getIcon(): Icon = TerraformIcons.Terraform

  override fun getDisplayName(): @Nls String = HCLBundle.message("terraform.deploy.name")
}

internal const val TF_DEPLOY_EXTENSION = "tfdeploy.hcl"
