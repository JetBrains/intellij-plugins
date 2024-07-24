// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.documentation

import com.intellij.model.Pointer
import com.intellij.openapi.components.service
import com.intellij.openapi.options.advanced.AdvancedSettings
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import com.intellij.util.applyIf
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLIdentifier

internal abstract class BaseTerraformDocumentationProvider {

  protected fun computeDocumentationTarget(element: PsiElement): DocumentationTarget? {
    if (element !is HCLElement) return null
    if (element is HCLIdentifier && TerraformPatterns.RootBlock.accepts(element.parent)) return null //TODO IJPL-158379
    return TerraformDocumentationTarget(element.createSmartPointer())
  }

  protected class TerraformDocumentationTarget(private val pointer: SmartPsiElementPointer<PsiElement>) : DocumentationTarget {

    override fun createPointer(): Pointer<out DocumentationTarget> = Pointer.delegatingPointer(pointer) {
      TerraformDocumentationTarget(it.createSmartPointer())
    }

    override fun computePresentation(): TargetPresentation {
      return TargetPresentation.builder(getHelpWindowHeader(pointer.element))
        .icon(TerraformIcons.Terraform)
        .presentation()
    }

    override fun computeDocumentationHint(): String {
      val element = pointer.element ?: return NO_DOC
      return LocalTfDocumentationProvider.fetchLocalDescription(element) ?: NO_DOC
    }

    override fun computeDocumentation(): DocumentationResult? {
      val element = pointer.element ?: return null
      val project = pointer.project
      val remoteDocProvider = project.service<RemoteTfDocumentationProvider>()
      val mdDocUrlProvider = project.service<TerraformMdDocUrlProvider>()
      val localDescription = element.let { LocalTfDocumentationProvider.fetchLocalDescription(it) }

      return DocumentationResult.Companion.asyncDocumentation {
        val docText = element.let { elem ->
          val urlString = if (shouldDownloadDocs) mdDocUrlProvider.getDocumentationUrl(elem).firstOrNull() else null
          urlString?.let { remoteDocProvider.getDoc(urlString) } ?: localDescription
        }

        DocumentationResult.documentation(docText ?: NO_DOC).applyIf(docText != null) {
          val externalUrl = TerraformWebDocUrlProvider.getDocumentationUrl(element).firstOrNull()
          val docAnchor = externalUrl?.substringAfterLast("#", ROOT_DOC_ANCHOR)
          externalUrl(externalUrl).anchor(docAnchor)
        }
      }
    }
  }
}

internal val shouldDownloadDocs: Boolean
  get() = AdvancedSettings.getBoolean("org.intellij.terraform.config.documentation.download")