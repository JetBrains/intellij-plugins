// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.JSElementVisitor
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSSuppressionHolder
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterList
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterListOwner
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.psi.impl.JSStubElementImpl
import com.intellij.psi.*
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.isScriptSetupTag
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.lang.expr.stub.VueJSEmbeddedExpressionContentStub
import org.jetbrains.vuejs.lang.html.psi.impl.VueScriptSetupEmbeddedContentImpl

class VueJSEmbeddedExpressionContentImpl :
  JSStubElementImpl<VueJSEmbeddedExpressionContentStub>, JSSuppressionHolder, VueJSEmbeddedExpressionContent,
  HintedReferenceHost, TypeScriptTypeParameterListOwner {

  constructor(node: ASTNode) : super(node)

  constructor(stub: VueJSEmbeddedExpressionContentStub, type: IStubElementType<*, *>) : super(stub, type)

  override fun getLanguage(): Language {
    return elementType.language
  }

  override fun accept(visitor: PsiElementVisitor) {
    if (visitor is JSElementVisitor) {
      visitor.visitJSEmbeddedContent(this)
    }
    else {
      super.accept(visitor)
    }
  }

  override fun allowTopLevelThis(): Boolean {
    return true
  }

  override fun subtreeChanged() {
    super.subtreeChanged()
    JSControlFlowService.getService(project).resetControlFlow(this)
  }

  override fun getQuoteChar(): Char? {
    return JSEmbeddedContentImpl.getQuoteChar(this)
  }

  override fun getReferences(hints: PsiReferenceService.Hints): Array<PsiReference> = PsiReference.EMPTY_ARRAY

  override fun shouldAskParentForReferences(hints: PsiReferenceService.Hints): Boolean = false

  override fun toString(): String {
    return super.toString() + "(${language.id})"
  }

  override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
    // In case of expressions on <script setup> tag, we need to process declarations in script setup contents as well
    parentOfType<XmlTag>()
      ?.takeIf { it.isScriptSetupTag() }
      ?.let { PsiTreeUtil.getStubChildOfType(it, JSEmbeddedContent::class.java) }
      ?.processDeclarations(processor, state, lastParent, place)
      ?.let {
        if (!it) return false
      }
    return super.processDeclarations(processor, state, lastParent, place)
  }

  override fun getTypeParameterList(): TypeScriptTypeParameterList? =
    (findModule(this, true) as? VueScriptSetupEmbeddedContentImpl)
      ?.typeParameterList

}
