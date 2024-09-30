package org.jetbrains.vuejs.libraries.nuxt.codeInsight

import com.intellij.lang.ecmascript6.psi.ES6ImportCall
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptIndexedAccessType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.typescript.psi.TypeScriptPsiUtil
import com.intellij.lang.typescript.resolve.TypeScriptCompilerTypePatcher
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.context.hasNuxt
import org.jetbrains.vuejs.index.VUE_FILE_EXTENSION

class NuxtTypeScriptTypePatcher : TypeScriptCompilerTypePatcher {
  override fun adjustDeclarationTypeFromServer(type: JSType, element: PsiElement): JSType? {
    if (type is JSAnyType && isNuxtComponentGlobalDeclaration(element) && hasNuxt(element)) {
      return null
    }
    return type
  }

  private fun isNuxtComponentGlobalDeclaration(element: PsiElement): Boolean =
    element is TypeScriptSingleType &&
    element.context is TypeScriptIndexedAccessType &&
    TypeScriptPsiUtil.isTypeofImport(element) &&
    PsiTreeUtil.getStubChildOfType(element, ES6ImportCall::class.java)?.referenceText
      ?.let(StringUtil::unquoteString)?.endsWith(VUE_FILE_EXTENSION) == true
}