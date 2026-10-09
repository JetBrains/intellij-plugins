// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.expr.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptVariableImpl
import com.intellij.lang.javascript.psi.impl.JSParameterImpl
import com.intellij.lang.javascript.psi.util.JSDestructuringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
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

  override fun getJSType(): JSType? =
    CachedValuesManager.getCachedValue(this) {
      var type = calculateDeclarationTypeStubSafe()
      if (type == null) {
        val inferredOrFromDestructuringType =
          TypeScriptVariableImpl.calculateDestructuringTypeStubSafe(this)
          ?: JSDestructuringUtil.getTypeFromInitializer(this) {
            getSlotTypeFromContext(this)
          }
        type = inferredOrFromDestructuringType?.asCompleteType()
      }
      CachedValueProvider.Result.create(type, this)
    }
}
