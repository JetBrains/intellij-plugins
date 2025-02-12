// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import org.intellij.terraform.config.Constants.REGISTRY_DOMAIN
import org.intellij.terraform.config.model.TypeModel

internal object TfWebDocUrlProvider : BaseTfDocUrlProvider() {

  private const val PROVIDERS_WEB_DOCS: String = "https://${REGISTRY_DOMAIN}/providers"

  override suspend fun getDocUrl(blockData: BlockData, context: String): String? {
    val (org, provider, version) = blockData.provider ?: return null
    val baseDocUrl = "$PROVIDERS_WEB_DOCS/${org}/${provider}/${version}/docs"
    return when (context) {
      PROVIDER -> "$baseDocUrl${blockData.parameter?.let { "#$it" } ?: ""}"
      RESOURCES, DATASOURCES -> "$baseDocUrl/$context/${blockData.identifier?.let { TypeModel.getResourceName(it) } ?: return null}" +
                                (blockData.parameter?.let { "#$it" } ?: "")
      else -> null
    }
  }
}