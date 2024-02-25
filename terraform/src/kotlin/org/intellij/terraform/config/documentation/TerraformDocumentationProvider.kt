// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.intellij.markdown.utils.convertMarkdownToHtml
import com.intellij.model.Pointer
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.coroutineToIndicator
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.io.HttpRequests
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.config.psi.TerraformDocumentPsi
import org.intellij.terraform.hcl.HCLLanguage
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLProperty
import org.jetbrains.annotations.Nls
import java.util.concurrent.TimeUnit


internal class TerraformDocumentationProvider : DocumentationTargetProvider, PsiDocumentationTargetProvider {

  private val docCache = Caffeine.newBuilder()
    .expireAfterAccess(5, TimeUnit.MINUTES)
    .build(::fetchDocumentationText)

  override fun documentationTargets(file: PsiFile, offset: Int): MutableList<out DocumentationTarget> {
    if (!file.language.`is`(HCLLanguage)) return mutableListOf()
    val element = file.findElementAt(offset) ?: return mutableListOf()

    val documentationTarget = documentationTarget(element, element.originalElement)
    if (documentationTarget == null) return mutableListOf()
    else return mutableListOf(documentationTarget)
  }

  override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
    if (element !is HCLElement) return null
    val file = element.containingFile
    if (file == null) return null
    return TerraformDocumentationTarget(element, docCache)
  }

  private class TerraformDocumentationTarget(val element: PsiElement, val docCache: LoadingCache<String, String>) : DocumentationTarget {

    override fun createPointer(): Pointer<out DocumentationTarget> = Pointer.hardPointer(this)

    override fun computePresentation(): TargetPresentation {
      return TargetPresentation.builder(getHelpWindowHeader(element))
        .icon(TerraformIcons.Terraform)
        .presentation()
    }

    override fun computeDocumentationHint(): String {
      return fetchLocalDescription(element)
    }

    override fun computeDocumentation(): DocumentationResult = DocumentationResult.Companion.asyncDocumentation {
      val project = readAction { element.project }
      val mdDocProvider = project.service<TerraformMdDocUrlProvider>()

      val doc = runCatching {
        val urlString = mdDocProvider.getDocumentationUrl(element).filterNotNull().first()
        coroutineToIndicator {
          convertMarkdownToHtml(docCache.get(urlString))
        }
      }.getOrElse {
        fileLogger().warnWithDebug("Cannot fetch documentation for element: ${element::class.java}, getting local metadata. Exception message:  ${it::class.java}: ${it.message} Enable DEBUG log level to see stack trace", it)
        readAction { fetchLocalDescription (element) }
      }

      val externalUrl = getWebUrl(element)
      val docAnchor = externalUrl?.substringAfterLast("#", "") ?: ""
      DocumentationResult.documentation(doc)
        .externalUrl(externalUrl)
        .anchor(docAnchor)
    }

    @Nls
    private fun fetchLocalDescription(element: PsiElement): String {
      return when (element) {
               is HCLProperty -> {
                 provideDocForProperty(element)
               }
               is HCLBlock -> {
                 provideDocForHclBlock(element)
               } //Block parameters
               is HCLIdentifier -> {
                 provideDocForIdentifier(element)
               } //Workaround for documentation - we do not parse type identifier in top-level blocks
               is TerraformDocumentPsi -> {
                 getBlockForDocumentationLink(element, element.name)?.let { provideDocForHclBlock(it) }
               }
               else -> NO_DOC
             } ?: NO_DOC
    }

    private suspend fun getWebUrl(element: PsiElement): String? {
      val terraformWebDocUrlProvider = readAction { element.project.service<TerraformWebDocUrlProvider>() }
      return terraformWebDocUrlProvider.getDocumentationUrl(element).firstOrNull()
    }

  }

  private fun fetchDocumentationText(urlString: String): String  {
    return removeMdHeader(
      HttpRequests.request(urlString)
        .connectTimeout(READ_TIMEOUT)
        .readTimeout(READ_TIMEOUT)
        .readString(ProgressManager.getInstance().progressIndicator)
    )
  }

  private fun removeMdHeader(input: String): String {
    var inFrontMatter = false
    val filteredLines = input.lineSequence().filter { line ->
      when { // Detect the start or end of a YAML front matter block
        line.trim() == "---" -> {
          inFrontMatter = !inFrontMatter
          false // Exclude this line
        } // Exclude lines that are within a YAML front matter block
        inFrontMatter -> false // Include all other lines
        else -> true
      }
    }
    return filteredLines.joinToString("\n").trim()
  }

}
