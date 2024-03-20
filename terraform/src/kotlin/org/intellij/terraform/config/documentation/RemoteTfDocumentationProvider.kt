// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.github.benmanes.caffeine.cache.Caffeine
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.coroutineToIndicator
import com.intellij.openapi.util.NlsSafe
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.HttpRequests
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.intellij.markdown.IElementType
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.terraform.config.documentation.html.TfFlavourDescriptor
import java.io.IOException
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
class RemoteTfDocumentationProvider(private val coroutineScope: CoroutineScope) {

  private val embeddedHtmlType = IElementType("ROOT")

  private val docCache = Caffeine.newBuilder()
    .expireAfterAccess(5, TimeUnit.MINUTES)
    .executor(AppExecutorUtil.getAppExecutorService())
    .build<String, Deferred<String?>>(::fetchDocumentationText)


  @NlsSafe
  suspend fun getDoc(urlString: String): String? = docCache.get(urlString).await()

  private fun fetchDocumentationText(urlString: String): Deferred<String?> {
    return coroutineScope.async(Dispatchers.IO) {
      coroutineToIndicator {
        try {
          val rawDocText = HttpRequests.request(urlString)
            .connectTimeout(FETCH_TIMEOUT)
            .readTimeout(FETCH_TIMEOUT)
            .readString(ProgressManager.getGlobalProgressIndicator())
          convertMarkdownToHtml(removeMdHeader(rawDocText))
        }
        catch (ex: IOException) {
          fileLogger().warnWithDebug("Cannot fetch documentation for url: ${urlString}, Exception: ${ex::class.java}: ${ex.message}. Enable DEBUG log level to see stack trace", ex)
          null
        }
      }
    }
  }

  private fun removeMdHeader(input: String): String {
    var inFrontMatter = false
    return input.lineSequence()
      .dropWhile { line ->
        if (line.trim() == "---") inFrontMatter = !inFrontMatter
        inFrontMatter
      }
      .joinToString("\n")
      .trim()
  }

  private fun convertMarkdownToHtml(@NlsSafe markdownText: String): String {
    val flavour = TfFlavourDescriptor(embeddedHtmlType)
    val parsedTree = MarkdownParser(flavour).parse(embeddedHtmlType, markdownText)
    return HtmlGenerator(markdownText, parsedTree, flavour).generateHtml()
  }

}