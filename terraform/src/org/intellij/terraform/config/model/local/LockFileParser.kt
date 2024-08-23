// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.local

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.SyntaxTraverser
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.LATEST_VERSION
import org.intellij.terraform.config.Constants.PROVIDER_VERSION
import org.intellij.terraform.config.model.ProviderType
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLObject

internal fun collectProviders(lockFile: PsiFile): Map<String, ProviderInfo> = SyntaxTraverser.psiTraverser(lockFile)
  .filter(HCLBlock::class.java)
  .filter { it.nameElements[0].text == HCL_PROVIDER_IDENTIFIER }
  .filterNotNull()
  .map { block -> ProviderInfo(block) }
  .associateBy { info -> info.fullName }

internal class ProviderInfo(block: HCLBlock) {

  val name: String?
  val namespace: String?
  val fullName: String
  val version: String

  init {
    val coordinates = ProviderType.parseCoordinates(block.name)
    this.name = coordinates.name
    this.namespace = coordinates.namespace
    fullName = "$namespace/$name"
    version = if (block.`object` != null && block.`object` is HCLObject) {
      val providerVersion = (block.`object` as HCLObject).propertyList.firstOrNull { it.name == PROVIDER_VERSION }?.value?.text
      StringUtil.unquoteString(providerVersion ?: LATEST_VERSION)
    }
    else {
      LATEST_VERSION
    }
  }
}
