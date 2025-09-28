// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.libraries.nuxt.codeInsight

import com.intellij.javascript.types.TSObjectType
import com.intellij.javascript.types.TSType
import com.intellij.lang.ecmascript6.psi.ES6ImportCall
import com.intellij.lang.javascript.psi.ecma6.TypeScriptIndexedAccessType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.typescript.psi.TypeScriptPsiUtil
import com.intellij.lang.typescript.resolve.TypeScriptCompilerTypeFilter
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.context.hasNuxt
import org.jetbrains.vuejs.index.VUE_FILE_EXTENSION

class NuxtTypeScriptTypeFilter : TypeScriptCompilerTypeFilter {
  override fun isAcceptableType(type: TSType, element: PsiElement): Boolean =
    !(isAdjustableType(type) && isNuxtComponentGlobalDeclaration(element) && hasNuxt(element))

  private fun isAdjustableType(type: TSType): Boolean =
    if (type is TSObjectType)
      isVueFilePath(type.symbol?.escapedName)
    else
      type is JSAnyType

  private fun isNuxtComponentGlobalDeclaration(element: PsiElement): Boolean =
    element is TypeScriptSingleType &&
    element.context is TypeScriptIndexedAccessType &&
    TypeScriptPsiUtil.isTypeofImport(element) &&
    isVueFilePath(PsiTreeUtil.getStubChildOfType(element, ES6ImportCall::class.java)?.referenceText)

  private fun isVueFilePath(path: String?): Boolean =
    path?.let(StringUtil::unquoteString)?.endsWith(VUE_FILE_EXTENSION) == true
}
