// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSExecutionScope
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService
import com.intellij.psi.impl.source.html.HtmlStubBasedTagImpl
import com.intellij.psi.impl.source.xml.stub.XmlTagStubImpl
import com.intellij.psi.stubs.IStubElementType

class VueTemplateTagImpl : HtmlStubBasedTagImpl, JSExecutionScope {
  constructor(stub: XmlTagStubImpl, nodeType: IStubElementType<out XmlTagStubImpl?, out HtmlStubBasedTagImpl?>) : super(stub, nodeType)
  constructor(node: ASTNode) : super(node)

  override fun isTopmostControlFlowScope() = true

  override fun subtreeChanged() {
    super.subtreeChanged()
    JSControlFlowService.getService(project).resetControlFlow(this)
  }
}
