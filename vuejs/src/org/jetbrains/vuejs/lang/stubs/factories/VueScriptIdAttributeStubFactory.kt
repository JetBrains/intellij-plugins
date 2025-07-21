// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs.factories

import com.intellij.lang.ASTNode
import com.intellij.lang.stubs.XmlStubBasedAttributeStubFactory
import com.intellij.psi.xml.XmlAttribute
import org.jetbrains.vuejs.lang.html.parser.VueElementTypes.SCRIPT_ID_ATTRIBUTE

class VueScriptIdAttributeStubFactory : XmlStubBasedAttributeStubFactory(SCRIPT_ID_ATTRIBUTE) {
  override fun shouldCreateStub(node: ASTNode): Boolean {
    return (node.psi as? XmlAttribute)?.parent
      ?.getAttribute("type")?.value == "text/x-template"
  }
}