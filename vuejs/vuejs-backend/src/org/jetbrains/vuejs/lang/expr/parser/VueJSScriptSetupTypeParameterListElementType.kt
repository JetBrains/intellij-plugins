// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSElementType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterList
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptTypeParameterListImpl
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.lang.expr.parser.VueJSElementTypes.EXTERNAL_ID_PREFIX

class VueJSScriptSetupTypeParameterListElementType : JSElementType<TypeScriptTypeParameterList>(
  "SCRIPT_SETUP_TYPE_PARAMETER_LIST") {
  override fun construct(node: ASTNode): PsiElement {
    return TypeScriptTypeParameterListImpl(node)
  }

  override fun toString(): String = EXTERNAL_ID_PREFIX + debugName
}
