// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.intellij.model.Pointer
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.openapi.options.advanced.AdvancedSettings
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import com.intellij.util.applyIf
import org.intellij.terraform.config.codeinsight.TfCompletionUtil
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLIdentifier

internal abstract class BaseTfDocumentationProvider {

  protected fun computeDocumentationTarget(element: PsiElement): DocumentationTarget? {
    if (element !is HCLElement) return null
    if (element is HCLIdentifier && TfPsiPatterns.RootBlock.accepts(element.parent)) return null //TODO IJPL-158379
    return TfDocumentationTarget(element.createSmartPointer())
  }

  protected class TfDocumentationTarget(private val pointer: SmartPsiElementPointer<PsiElement>) : DocumentationTarget {

    override fun createPointer(): Pointer<out DocumentationTarget> = Pointer.delegatingPointer(pointer) {
      TfDocumentationTarget(it.createSmartPointer())
    }

    override fun computePresentation(): TargetPresentation {
      return TargetPresentation.builder(getHelpWindowHeader(pointer.element))
        .icon(pointer.element?.let {TfCompletionUtil.getLookupIcon(it)})
        .presentation()
    }

    override fun computeDocumentationHint(): String {
      return pointer.element?.let { LocalTfDocumentationProvider.fetchLocalDescription(it) } ?: NO_DOC
    }

    override fun computeDocumentation(): DocumentationResult? {
      val project = pointer.project
      val remoteDocProvider = project.service<RemoteTfDocumentationProvider>()
      val mdDocUrlProvider = project.service<TfMdDocUrlProvider>()

      return DocumentationResult.Companion.asyncDocumentation {
        val urlString = if (shouldDownloadDocs) mdDocUrlProvider.getDocumentationUrl(pointer).firstOrNull() else null
        val docText = urlString?.let { remoteDocProvider.getDoc(urlString) }
                      ?: readAction { LocalTfDocumentationProvider.fetchLocalDescription(pointer.element) }

        DocumentationResult.documentation(docText ?: NO_DOC).applyIf(docText != null) {
          val externalUrl = TfWebDocUrlProvider.getDocumentationUrl(pointer).firstOrNull()
          val docAnchor = externalUrl?.substringAfterLast("#", ROOT_DOC_ANCHOR)
          externalUrl(externalUrl).anchor(docAnchor)
        }
      }
    }
  }
}

internal val shouldDownloadDocs: Boolean
  get() = AdvancedSettings.getBoolean("org.intellij.terraform.config.documentation.download")