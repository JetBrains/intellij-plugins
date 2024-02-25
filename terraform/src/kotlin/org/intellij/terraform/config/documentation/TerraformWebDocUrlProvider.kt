// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.intellij.openapi.components.Service
import com.intellij.psi.PsiElement
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
internal class TerraformWebDocUrlProvider(val coroutineScope: CoroutineScope) : TerraformDocUrlProvider() {

  private val PROVIDERS_WEB_DOCS: String = "https://registry.terraform.io/providers"

  override suspend fun getProviderDocUrl(provider: String, element: PsiElement, property: String?): String? {
    val (org, providerStr, version) = buildProviderInfo(element, provider, PROVIDER) ?: return null
    return "$PROVIDERS_WEB_DOCS/${org}/${providerStr}/${version}/docs" + (property?.let { "#$it" } ?: "")
  }

  override suspend fun getResourceOrDataSourceDocUrl(identifier: String, type: String, element: PsiElement, property: String?): String? {
    val (org, provider, version) = buildProviderInfo(element, identifier, type) ?: return null
    val id = getResourceId(identifier)
    return "$PROVIDERS_WEB_DOCS/${org}/${provider}/${version}/docs/${type}/${id}" + (property?.let { "#$it" } ?: "")
  }
}