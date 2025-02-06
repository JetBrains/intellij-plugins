// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.impl.JSVariableImpl
import com.intellij.lang.javascript.psi.stubs.JSVariableStub
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.parentOfType
import org.angular2.codeInsight.blocks.BLOCK_LET
import org.angular2.lang.expr.parser.Angular2StubElementTypes
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.lang.types.Angular2BlockVariableType

class Angular2BlockParameterVariableImpl : JSVariableImpl<JSVariableStub<in JSVariable>, JSVariable> {
  constructor(node: ASTNode) : super(node)
  constructor(stub: JSVariableStub<JSVariable>) : super(stub, Angular2StubElementTypes.BLOCK_PARAMETER_VARIABLE)

  override fun calculateType(): JSType {
    return Angular2BlockVariableType(this)
  }

  override fun getJSType(): JSType? {
    return CachedValuesManager.getCachedValue(this) {
      CachedValueProvider.Result.create(calculateType(), PsiModificationTracker.MODIFICATION_COUNT)
    }
  }

  override fun hasBlockScope(): Boolean =
    true

  override fun getDeclarationScope(): PsiElement? =
    parentOfType<Angular2HtmlBlock>()
      ?.let { if (it.name == BLOCK_LET) containingFile else it }

  override fun delete() {
    parentOfType<Angular2HtmlBlock>()
      ?.takeIf { it.name == BLOCK_LET }
      ?.let {
        it.delete()
        return
      }
    val blockParameter = parent.parent as? Angular2BlockParameter
    super.delete()
    if (blockParameter?.variables?.isEmpty() == true) {
      blockParameter.delete()
    }
  }

  override fun isLocal(): Boolean {
    return true
  }

  override fun isConst(): Boolean {
    return true
  }

  override fun hasInitializer(): Boolean {
    return true
  }

  override fun calcAccessType(): JSAttributeList.AccessType {
    return JSAttributeList.AccessType.PUBLIC
  }

  override fun useTypesFromJSDoc(): Boolean {
    return false
  }
}