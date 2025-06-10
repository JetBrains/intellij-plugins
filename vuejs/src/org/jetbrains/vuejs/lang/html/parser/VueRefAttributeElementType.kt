// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.xml.stub.XmlStubBasedElementType
import com.intellij.psi.tree.ICompositeElementType
import com.intellij.psi.xml.IXmlAttributeElementType
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.psi.impl.VueRefAttributeImpl

class VueRefAttributeElementType :
  XmlStubBasedElementType<VueRefAttributeImpl>("REF_ATTRIBUTE", VueLanguage.INSTANCE),
  ICompositeElementType, IXmlAttributeElementType {

  override fun createPsi(node: ASTNode): VueRefAttributeImpl {
    return VueRefAttributeImpl(node)
  }
}