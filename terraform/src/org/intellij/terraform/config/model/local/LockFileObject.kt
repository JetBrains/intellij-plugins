// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.local

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.SyntaxTraverser
import com.intellij.util.concurrency.annotations.RequiresReadLock
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.Constants.LATEST_VERSION
import org.intellij.terraform.config.Constants.REGISTRY_DOMAIN
import org.intellij.terraform.config.Constants.TERRAFORM_DOMAIN
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLObject


internal class LockFileObject private constructor (lockFile: PsiFile) {

  companion object {
    internal const val VERSION: String = "version"

    @RequiresReadLock
    fun create(lockFile: PsiFile): LockFileObject {
      return LockFileObject(lockFile)
    }
  }

  internal class ProviderInfo(block: HCLBlock) {

    val name: String?
    val namespace: String?
    val fullName: String
    val version: String

    init {
      val strings = block.name.split("/").takeIf { it.size == 3 && it[0] == REGISTRY_DOMAIN || it[0] == TERRAFORM_DOMAIN }
      name = strings?.let { it[2] }
      namespace = strings?.let { it[1] }
      fullName = "$namespace/$name"
      version = if (block.`object` != null && block.`object` is HCLObject) {
        val providerVersion = (block.`object` as HCLObject).propertyList.firstOrNull { it.name == VERSION }?.value?.text
        StringUtil.unquoteString(providerVersion ?: LATEST_VERSION)
      }
      else {
        LATEST_VERSION
      }
    }
  }

  val providers: Map<String, ProviderInfo> = SyntaxTraverser.psiTraverser(lockFile)
    .filter(HCLBlock::class.java)
    .filter { it.nameElements[0].text == HCL_PROVIDER_IDENTIFIER }
    .filterNotNull()
    .map {block -> ProviderInfo(block) }
    .associateBy { info -> info.fullName }
}