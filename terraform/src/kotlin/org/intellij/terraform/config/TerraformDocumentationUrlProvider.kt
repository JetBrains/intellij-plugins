// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.psi.PsiElement

object TerraformDocumentationUrlProvider {

  private fun getProviderOrg(provider: String, element: PsiElement): String {
    //TODO Use LocalSchemaService.findLockFile(virtualFile) to find info about providers
    return "hashicorp"
  }

  private fun getProviderVersion(provider: String, element: PsiElement): String {
    //TODO Use LocalSchemaService.findLockFile(virtualFile) to find info about provider version
    return "latest"
  }

  //https://registry.terraform.io/providers/hashicorp/aws/latest/docs
  @JvmStatic
  fun getProviderUrl(provider: String, element: PsiElement, property: String? = null): String {
    val org = getProviderOrg(provider, element)
    return "https://registry.terraform.io/providers/${org}/${provider}/latest/docs" + (property?.let { "#$it" } ?: "")
  }

  //https://registry.terraform.io/providers/hashicorp/$PROVIDER/$VERSION/docs/$TYPE/$NAME
  @JvmStatic
  fun getResourceOrDataSourceUrl(identifier: String, type: String, element: PsiElement, property: String? = null): String {
    val stringList = identifier.split("_", limit = 2)
    val (provider, id) = if (stringList.size < 2) Pair(identifier, identifier) else Pair(stringList[0], stringList[1])
    val version = getProviderVersion(provider, element)
    val org = getProviderOrg(provider, element)
    return "https://registry.terraform.io/providers/${org}/${provider}/${version}/docs/${type}/${id}" + (property?.let { "#$it" } ?: "")
  }
}