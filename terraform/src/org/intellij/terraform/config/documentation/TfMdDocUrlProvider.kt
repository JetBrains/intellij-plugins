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
import com.intellij.util.applyIf
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.HttpRequests
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.intellij.terraform.config.Constants.LATEST_VERSION
import org.intellij.terraform.config.Constants.REGISTRY_DOMAIN
import org.intellij.terraform.config.model.TypeModel
import java.io.IOException
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
internal class TfMdDocUrlProvider(private val coroutineScope: CoroutineScope) : BaseTfDocUrlProvider() {

  companion object {
    private const val PROVIDERS_REGISTRY_URL: String = "https://${REGISTRY_DOMAIN}/v1/providers"
    private const val GITHUB_RAW_FILES_URL: String = "https://raw.githubusercontent.com/"
    private const val GITHUB_PREFIX: String = "https://github.com/"
  }

  private val providerInfoCache = Caffeine.newBuilder()
    .expireAfterAccess(5, TimeUnit.MINUTES)
    .executor(AppExecutorUtil.getAppExecutorService())
    .build<String, Deferred<TfProviderInfo?>>(::loadProviderInfo)

  private val mapper = ObjectMapper(JsonFactory()).registerModule(KotlinModule.Builder().build())

  override suspend fun getDocUrl(blockData: BlockData, context: String): String? {
    val providerData = blockData.provider?.let { fetchProviderData(it) } ?: return null
    return when (context) {
      PROVIDER -> providerData.docs.firstOrNull { it.category == "overview" }
      RESOURCES, DATASOURCES -> providerData.docs.firstOrNull {
        it.category == context && (it.title == blockData.identifier?.let { TypeModel.getResourceName(it) } || it.title == blockData.identifier)
      }
      else -> null
    }?.let { docMetadata ->
      "${providerData.source.replace(GITHUB_PREFIX, GITHUB_RAW_FILES_URL)}/${providerData.tag}/${docMetadata.path}"
    }
  }

  private suspend fun fetchProviderData(info: ProviderData): TfProviderInfo? {
    val (org, provider, version) = info
    val metadataUrl = "$PROVIDERS_REGISTRY_URL/${org}/${provider}".applyIf(version != LATEST_VERSION) { plus("/${version}") }
    return providerInfoCache.get(metadataUrl).await()
  }

  private fun loadProviderInfo(metadataUrl: String): Deferred<TfProviderInfo?> {
    return coroutineScope.async(Dispatchers.IO) {
      coroutineToIndicator {
        try {
          val response = HttpRequests.request(metadataUrl)
            .connectTimeout(FETCH_TIMEOUT)
            .readTimeout(FETCH_TIMEOUT)
            .readString(ProgressManager.getGlobalProgressIndicator())
          mapper.reader().readValue(response, TfProviderInfo::class.java)
        }
        catch (ex: IOException) {
          fileLogger().warnWithDebug("Cannot fetch terraform provider info from ${metadataUrl}: ${ex::class.java}: ${ex.message} Enable DEBUG log level to see stack trace", ex)
          null
        }
      }
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private class TfProviderDocInfo(val title: String, val category: String, val path: String)

  @JsonIgnoreProperties(ignoreUnknown = true)
  private class TfProviderInfo(val source: String, val tag: String, val docs: List<TfProviderDocInfo>)

}