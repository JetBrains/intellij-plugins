// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.benmanes.caffeine.cache.Caffeine
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.coroutineToIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.applyIf
import com.intellij.util.io.HttpRequests
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
internal class TerraformMdDocUrlProvider(val project: Project) : TerraformDocUrlProvider() {

  private val PROVIDERS_REGISTRY_URL: String = "https://registry.terraform.io/v1/providers"
  private val GITHUB_RAW_FILES_URL: String = "https://raw.githubusercontent.com/"
  private val GITHUB_PREFIX: String = "https://github.com/"

  private val providerInfoCache = Caffeine.newBuilder()
    .expireAfterAccess(5, TimeUnit.MINUTES)
    .build<String, TerraformProviderInfo?>(::loadProviderInfo)

  override suspend fun getProviderDocUrl(provider: String, element: PsiElement, property: String?): String? {
    val providerData = buildProviderInfo(element, provider, PROVIDER)?.let { coroutineToIndicator {  fetchProviderData(it) } } ?: return null
    val docMetadata = providerData.docs.firstOrNull { it.category == "overview" } ?: return null
    return "${providerData.source.replace(GITHUB_PREFIX, GITHUB_RAW_FILES_URL)}/${providerData.tag}/${docMetadata.path}"
  }

  override suspend fun getResourceOrDataSourceDocUrl(identifier: String, type: String, element: PsiElement, property: String?): String? {
    val providerData = buildProviderInfo(element, identifier, type)?.let { coroutineToIndicator {  fetchProviderData(it) } } ?: return null
    val id = getResourceId(identifier)
    val docMetadata = providerData.docs.firstOrNull { it.category == type && it.title == id } ?: return null
    return "${providerData.source.replace(GITHUB_PREFIX, GITHUB_RAW_FILES_URL)}/${providerData.tag}/${docMetadata.path}"
  }

  private fun fetchProviderData(info: ProviderData): TerraformProviderInfo? {
    val metadataUrl = "$PROVIDERS_REGISTRY_URL/${info.org}/${info.provider}".applyIf(info.version != LATEST_VERSION) { plus("/${info.version}") }
    return providerInfoCache.get(metadataUrl)
  }

  private fun loadProviderInfo(metadataUrl: String): TerraformProviderInfo? {
    return runCatching {
      val response = HttpRequests.request(metadataUrl).connectTimeout(READ_TIMEOUT).readString(ProgressManager.getInstance().progressIndicator)
      val mapper = ObjectMapper(JsonFactory()).registerModule(KotlinModule.Builder().build())
      mapper.reader().readValue(response, TerraformProviderInfo::class.java)
    }.getOrElse {
      fileLogger().warnWithDebug("Cannot fetch terraform provider info from ${metadataUrl}. Exception message:  ${it::class.java}: ${it.message} Enable DEBUG log level to see stack trace", it)
      null
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private class TerraformProviderDocInfo(val title: String, val category: String, val path: String)

  @JsonIgnoreProperties(ignoreUnknown = true)
  private class TerraformProviderInfo(val source: String, val tag: String, val docs: List<TerraformProviderDocInfo>)

}