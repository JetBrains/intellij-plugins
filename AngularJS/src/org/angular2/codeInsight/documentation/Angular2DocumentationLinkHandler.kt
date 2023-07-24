// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.documentation

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.codeInsight.documentation.DocumentationManagerProtocol
import com.intellij.lang.documentation.ide.impl.browseAbsolute
import com.intellij.lang.documentation.psi.PsiElementDocumentationTarget
import com.intellij.lang.documentation.psi.psiDocumentationTarget
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.model.Pointer
import com.intellij.openapi.util.component1
import com.intellij.openapi.util.component2
import com.intellij.platform.backend.documentation.DocumentationLinkHandler
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.LinkResolveResult
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import org.angular2.entities.Angular2EntitiesProvider

/**
 * Customizes handling of URLs in Angular 2 projects.
 *
 * - for [PsiElement]s from core Angular libraries, it prepends `https://angular.io/` to relative URLs
 * - allows to navigate to [PsiElement]s from [Angular2ElementDocumentationTarget]
 */
class Angular2DocumentationLinkHandler : DocumentationLinkHandler {
  override fun resolveLink(target: DocumentationTarget, url: String): LinkResolveResult? {
    if (url.none { it == ':' }) {
      val sourceElement = (target as? PsiElementDocumentationTarget)?.targetElement
                          ?: (target as? Angular2ElementDocumentationTarget)?.elements?.get(0)?.sourceElement
      if (sourceElement is JSElement) {
        val prefix = getExternalDocRelativeUrlPrefix(sourceElement)
        if (prefix != null) {
          LinkResolveResult.asyncResult {
            LinkResolveResult.Async.resolvedTarget(object : DocumentationTarget {
              override fun createPointer(): Pointer<out DocumentationTarget> =
                Pointer { null }

              override fun computePresentation(): TargetPresentation =
                TargetPresentation.builder("").presentation()
            })
          }
          browseAbsolute(sourceElement.project, prefix + url)
        }
      }
    }
    else if (url.startsWith(DocumentationManagerProtocol.PSI_ELEMENT_PROTOCOL)) {
      if (target is Angular2ElementDocumentationTarget) {
        val sourceElement = target.elements[0].sourceElement
        val (resolved, _) = DocumentationManager.targetAndRef(sourceElement.project, url, sourceElement)
                            ?: return null
        return LinkResolveResult.resolvedTarget(
          Angular2EntitiesProvider.getEntity(resolved)
            ?.let { Angular2ElementDocumentationTarget.create(it.getName(), null, it) }
          ?: psiDocumentationTarget(resolved, null)
        )
      }
    }
    return null
  }

}