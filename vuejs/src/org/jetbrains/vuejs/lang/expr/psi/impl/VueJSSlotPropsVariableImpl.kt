// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.impl.JSVariableImpl
import com.intellij.lang.javascript.psi.stubs.JSVariableStubBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.expr.psi.VueJSSlotPropsVariable

class VueJSSlotPropsVariableImpl(node: ASTNode?) : JSVariableImpl<JSVariableStubBase<JSVariable>, JSVariable>(
  node), VueJSSlotPropsVariable {
  override fun hasBlockScope(): Boolean = true

  override fun getDeclarationScope(): PsiElement? =
    PsiTreeUtil.getContextOfType(this, XmlTag::class.java, PsiFile::class.java)
}
