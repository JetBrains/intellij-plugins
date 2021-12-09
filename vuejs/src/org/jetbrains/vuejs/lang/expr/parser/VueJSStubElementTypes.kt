// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.types.JSParameterElementType
import com.intellij.lang.javascript.types.JSVariableElementType
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.lang.expr.psi.impl.VueJSScriptSetupParameterImpl
import org.jetbrains.vuejs.lang.expr.psi.impl.VueJSSlotPropsParameterImpl
import org.jetbrains.vuejs.lang.expr.psi.impl.VueJSVForVariableImpl

object VueJSStubElementTypes {

  val V_FOR_VARIABLE: JSVariableElementType = object : JSVariableElementType("V_FOR_VARIABLE") {
    override fun construct(node: ASTNode): PsiElement {
      return VueJSVForVariableImpl(node)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
      return false
    }
  }

  val SLOT_PROPS_PARAMETER: JSParameterElementType = object : JSParameterElementType("SLOT_PROPS_PARAMETER") {
    override fun construct(node: ASTNode): PsiElement {
      return VueJSSlotPropsParameterImpl(node)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
      return false
    }
  }

  val SCRIPT_SETUP_PARAMETER: JSParameterElementType = object : JSParameterElementType("SCRIPT_SETUP_PARAMETER") {
    override fun construct(node: ASTNode): PsiElement {
      return VueJSScriptSetupParameterImpl(node)
    }

    override fun shouldCreateStub(node: ASTNode): Boolean {
      return false
    }
  }
}
