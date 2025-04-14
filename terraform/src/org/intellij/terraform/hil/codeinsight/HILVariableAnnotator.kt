// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.codeinsight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.terraform.config.Constants.HCL_PROVIDER_IDENTIFIER
import org.intellij.terraform.config.codeinsight.TfCompletionUtil
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hcl.psi.HCLForObjectExpression
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.ProviderDefinedFunction
import org.intellij.terraform.hcl.psi.common.SelectExpression
import org.intellij.terraform.hil.HILElementTypes
import org.intellij.terraform.hil.HILSyntaxHighlighter
import org.intellij.terraform.hil.psi.getGoodLeftElement
import org.intellij.terraform.hil.psi.impl.getHCLHost

class HILVariableAnnotator : Annotator, DumbAware {
  private val DEBUG = ApplicationManager.getApplication().isUnitTestMode

  private val ellipsis = TokenSet.create(HILElementTypes.OP_ELLIPSIS, HCLElementTypes.OP_ELLIPSIS)
  private val commas = TokenSet.create(HILElementTypes.COMMA, HCLElementTypes.COMMA)
  private val rParens = TokenSet.create(HILElementTypes.R_PAREN, HCLElementTypes.R_PAREN)

  override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    if (ellipsis.contains(element.node?.elementType)) {
      if (element.parent is HCLForObjectExpression) {
        return
      }
      if (commas.contains(PsiTreeUtil.skipWhitespacesAndCommentsBackward(element)?.node?.elementType)) {
        holder.newAnnotation(HighlightSeverity.ERROR, HCLBundle.message("hil.variable.annotator.expression.start.expected")).create()
      } else if (!rParens.contains(PsiTreeUtil.skipWhitespacesAndCommentsForward(element)?.node?.elementType)) {
        holder.newAnnotation(HighlightSeverity.ERROR,
                             HCLBundle.message("hil.variable.annotator.expanded.function.argument.must.be.immediately.followed.by.closing.parentheses")).create()
      }
      return
    }

    if (isProviderDefinedFunction(element, holder)) {
      return
    }

    if (element !is BaseExpression) return
    val host = element.getHCLHost() ?: return
    val hostFile = host.containingFile
    if (hostFile !is HCLFile || !hostFile.isInterpolationsAllowed()) return

    if (element !is Identifier) return
    val parent = element.parent
    if (parent is SelectExpression<*>) {
      if (parent.from === element) {
        annotateLeftmostInSelection(element, holder)
      } else if (isScopeElementReference(element, parent)) {
        createInfo(holder, "scope value reference", HILSyntaxHighlighter.TIL_PROPERTY_REFERENCE)
      } else if (isResourceInstanceReference(element, parent)) {
        createInfo(holder, "resource instance reference",HILSyntaxHighlighter.TIL_RESOURCE_INSTANCE_REFERENCE)
      } else if (isResourcePropertyReference(element, parent)) {
        createInfo(holder, "property reference", HILSyntaxHighlighter.TIL_PROPERTY_REFERENCE)
      }
    }
  }

  private fun createInfo(holder: AnnotationHolder, @NlsSafe debug: String, textAttributesKey: TextAttributesKey) {
    if (DEBUG) {
      holder.newAnnotation(HighlightSeverity.INFORMATION, debug).textAttributes(textAttributesKey).create()
    }
    else {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(textAttributesKey).create()

    }
  }

  private fun annotateLeftmostInSelection(element: Identifier, holder: AnnotationHolder) {
    if (TfCompletionUtil.Scopes.contains(element.name)) {
      createInfo(holder, "global scope", HILSyntaxHighlighter.TIL_PREDEFINED_SCOPE)
    } else {
      createInfo(holder, "resource type reference", HILSyntaxHighlighter.TIL_RESOURCE_TYPE_REFERENCE)
    }
  }

  private fun isProviderDefinedFunction(element: PsiElement, holder: AnnotationHolder): Boolean {
    val parent = element.parent as? ProviderDefinedFunction<*> ?: return false
    if (element !is Identifier || parent.keyword !== element) return false

    if (element.text != HCL_PROVIDER_IDENTIFIER) {
      holder.newAnnotation(HighlightSeverity.ERROR, HCLBundle.message("hil.annotator.provider.defined.function.keyword")).create()
      return true
    }
    return false
  }
}

fun isScopeElementReference(element: Identifier, parent: SelectExpression<*>): Boolean {
  val left = getGoodLeftElement(parent, element, false) as? Identifier ?: return false
  val lp = left.parent as? SelectExpression<*> ?: return false
  return (left === lp.from) && TfCompletionUtil.Scopes.contains(left.name)
}

fun isResourceInstanceReference(element: Identifier, parent: SelectExpression<*>): Boolean {
  val left = getGoodLeftElement(parent, element, false) as? Identifier ?: return false
  val lp = left.parent as? SelectExpression<*> ?: return false
  return (left === lp.from) && !TfCompletionUtil.Scopes.contains(left.name)
}

fun isResourcePropertyReference(element: Identifier, parent: SelectExpression<*>): Boolean {
  // TODO: Improve. Somehow. See ILSelectFromSomethingReferenceProvider
  // Since this function called after another two, other variants are already used
  if (parent.from !is SelectExpression<*>) return false
  return getGoodLeftElement(parent, element) is Identifier
}