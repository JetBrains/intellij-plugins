// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.xml.stub.XmlTagStubImpl
import org.jetbrains.vuejs.lang.html.psi.impl.VueTemplateTagImpl

class VueTemplateTagElementType : VueStubBasedTagElementType("TEMPLATE_TAG") {
  override fun createPsi(node: ASTNode) = VueTemplateTagImpl(node)
  override fun createPsi(stub: XmlTagStubImpl) = VueTemplateTagImpl(stub, this)
}