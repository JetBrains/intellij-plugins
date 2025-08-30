// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr.highlighting

import com.intellij.codeInsight.daemon.RainbowVisitor
import com.intellij.codeInsight.daemon.impl.HighlightVisitor
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.lang.javascript.highlighting.JSSemanticHighlightingVisitor
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement
import com.intellij.lang.javascript.psi.impl.JSUseScopeProvider
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import org.angular2.entities.source.Angular2SourceUtil
import org.angular2.lang.Angular2HighlightDescriptor
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.html.Angular2HtmlFile

class Angular2RainbowVisitor : RainbowVisitor() {

  override fun suitableForFile(psiFile: PsiFile): Boolean =
    psiFile is Angular2HtmlFile

  override fun visit(element: PsiElement) {
    if ((element !is JSReferenceExpression && element !is JSVariable)
        || !Angular2LangUtil.isAngular2Context(element))
      return

    val target: PsiElement? =
      if (element is JSReferenceExpression
          && (element.getQualifier() == null || element.getQualifier() is JSThisExpression))
        element.resolve()
      else
        element


    if (target is JSVariable) {
      if (target.containingFile != element.containingFile &&
          Angular2SourceUtil.findComponentClass(element) != target.parentOfType<TypeScriptClass>()) {
        return
      }

      val highlight = if (element is JSReferenceExpression)
        element.getReferenceNameElement()
      else
        target.getNameIdentifier()

      val scope = JSUseScopeProvider.getLexicalScopeOrFile(target) ?: return
      val info = getInfo(scope, highlight ?: return, getName(target), getColorKey(target))
      addInfo(info)
    }
  }

  private fun getColorKey(element: PsiElement): TextAttributesKey? {
    Angular2HighlightDescriptor.getFor(element, element)
      ?.let { return it.attributesKey }
    return if (element is JSVariable) {
      if (JSSemanticHighlightingVisitor.isLocalVariable(element))
        JSHighlighter.JS_LOCAL_VARIABLE
      else
        JSHighlighter.JS_GLOBAL_VARIABLE
    }
    else null
  }

  /**
   * Must persist after edits, so can't use offset etc.
   */
  private fun getName(`var`: JSQualifiedNamedElement): String {
    val name = `var`.getName()
    return name ?: ""
  }

  override fun clone(): HighlightVisitor =
    Angular2RainbowVisitor()
}