// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs.factories

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.stubs.factories.JSStubFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.util.parentOfType
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.index.isScriptSetupTag
import org.jetbrains.vuejs.lang.expr.parser.VueJSEmbeddedExpressionContentElementType
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.lang.expr.stub.VueJSEmbeddedExpressionContentStub
import org.jetbrains.vuejs.lang.expr.stub.impl.VueJSEmbeddedExpressionContentStubImpl

internal class VueJSEmbeddedExpressionContentStubFactory(elementTypeSupplier: () -> VueJSEmbeddedExpressionContentElementType) : JSStubFactory<VueJSEmbeddedExpressionContentStub, VueJSEmbeddedExpressionContent>(elementTypeSupplier) {
  override fun createStub(psi: VueJSEmbeddedExpressionContent, parentStub: StubElement<out PsiElement>?): VueJSEmbeddedExpressionContentStub =
    VueJSEmbeddedExpressionContentStubImpl(parentStub, elementType)

  override fun shouldCreateStub(node: ASTNode): Boolean =
    node.psi.parentOfType<XmlTag>()?.isScriptSetupTag() == true
}