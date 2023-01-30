// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSSourceElement
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.AccessibilityProcessingHandler
import com.intellij.lang.typescript.TypeScriptSpecificHandlersFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.psi.util.parents
import org.jetbrains.astro.codeInsight.refs.AstroReferenceExpressionResolver
import org.jetbrains.astro.editor.AstroComponentSourceEdit
import org.jetbrains.astro.lang.AstroFileImpl
import org.jetbrains.astro.lang.psi.AstroContentRoot
import org.jetbrains.astro.lang.psi.AstroFrontmatterScript

class AstroSpecificHandlersFactory : TypeScriptSpecificHandlersFactory() {

  override fun createReferenceExpressionResolver(referenceExpression: JSReferenceExpressionImpl,
                                                 ignorePerformanceLimits: Boolean): ResolveCache.PolyVariantResolver<JSReferenceExpressionImpl> {
    return AstroReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits)
  }

  override fun createAccessibilityProcessingHandler(place: PsiElement?, skipNsResolving: Boolean): AccessibilityProcessingHandler {
    return AstroAccessibilityProcessingHandler(place)
  }

  override fun getExportScope(element: PsiElement): JSElement? {
    return super.getExportScope(element).let { if (it is AstroFrontmatterScript) it.context as? JSElement else it }
  }

  override fun findStatementAnchor(currentAnchor: JSSourceElement?, referenceExpression: PsiElement): PsiElement? {
    val astroFile = referenceExpression.containingFile as? AstroFileImpl ?: return currentAnchor
    if (referenceExpression.parents(false).any { it is AstroFrontmatterScript })
      return currentAnchor
    astroFile.frontmatterScript()?.node?.treeNext?.psi?.let { return it }

    var result: AstroFrontmatterScript? = null
    ApplicationManager.getApplication().assertIsDispatchThread()
    ApplicationManager.getApplication().runWriteAction {
      val commandProcessor = CommandProcessor.getInstance()
      val runnable = Runnable {
        result = AstroComponentSourceEdit(astroFile).getOrCreateFrontmatterScript()
      }
      commandProcessor.runUndoTransparentAction(runnable)
    }
    return result
  }

  override fun findFunctionAnchor(currentAnchor: PsiElement?, scope: PsiElement, originalAnchor: PsiElement): PsiElement? {
    if (scope !is AstroContentRoot) {
      return currentAnchor
    }
    val frontmatterScript = scope.frontmatterScript()
    if (frontmatterScript == null) {
      if (!ApplicationManager.getApplication().isDispatchThread) {
        // Cannot create frontmatter script
        return scope
      }
      ApplicationManager.getApplication().assertIsDispatchThread()
      ApplicationManager.getApplication().runWriteAction {
        val commandProcessor = CommandProcessor.getInstance()
        val runnable = Runnable {
          AstroComponentSourceEdit(scope.containingFile as AstroFileImpl).getOrCreateFrontmatterScript()
        }
        commandProcessor.runUndoTransparentAction(runnable)
      }
    }
    return scope.frontmatterScript()?.node?.treeNext
             ?.let { if (it.elementType == TokenType.WHITE_SPACE) it.treeNext else it }
             ?.psi
           ?: scope
  }

}