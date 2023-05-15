// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptVariableImpl
import com.intellij.lang.javascript.psi.impl.JSParameterImpl
import com.intellij.lang.javascript.psi.util.JSDestructuringUtil
import com.intellij.lang.typescript.psi.TypeScriptPsiUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.expr.psi.VueJSSlotPropsParameter
import org.jetbrains.vuejs.model.getSlotTypeFromContext
import org.jetbrains.vuejs.types.asCompleteType

class VueJSSlotPropsParameterImpl(node: ASTNode) : JSParameterImpl(node), VueJSSlotPropsParameter {
  override fun hasBlockScope(): Boolean = true

  override fun getUseScope(): SearchScope {
    return declarationScope?.let { LocalSearchScope(it) } ?: LocalSearchScope.EMPTY
  }

  override fun getDeclarationScope(): PsiElement? =
    PsiTreeUtil.getContextOfType(this, XmlTag::class.java, PsiFile::class.java)

  override fun calculateType(): JSType? {
    val type = calculateDeclaredType() ?: JSDestructuringUtil.getTypeFromInitializer(this) {
      getSlotTypeFromContext(this)
    }

    return type?.asCompleteType()
  }

  private fun calculateDeclaredType(): JSType? {
    if (!DialectDetector.isTypeScript(this)) return null

    return TypeScriptPsiUtil.getTypeFromDeclaration(this)
           ?: TypeScriptVariableImpl.calculateDestructuringTypeStubSafe(this)
  }

}
