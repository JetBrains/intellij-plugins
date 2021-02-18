// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.html.HtmlStubBasedTagElementType
import com.intellij.psi.impl.source.xml.stub.XmlAttributeStubImpl
import com.intellij.psi.impl.source.xml.stub.XmlStubBasedAttributeElementType
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.PathUtil
import org.jetbrains.vuejs.index.VueIdIndex
import org.jetbrains.vuejs.index.VueUrlIndex
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.model.SLOT_TAG_NAME

object VueStubElementTypes {

  val STUBBED_TAG = object : HtmlStubBasedTagElementType("STUBBED_TAG", VueLanguage.INSTANCE) {
    override fun shouldCreateStub(node: ASTNode?): Boolean {
      return (node?.psi as? XmlTag)
               ?.let {
                 // top-level style/script/template tag
                 it.parentTag == null
                 // slot tag
                 || it.name == SLOT_TAG_NAME
                 // script tag with x-template
                 || (it.getAttributeValue("type") == "text/x-template"
                     && it.getAttribute("id") != null)
               } ?: false
    }
  }

  val SCRIPT_ID_ATTRIBUTE = object : XmlStubBasedAttributeElementType("SCRIPT_ID_ATTRIBUTE", VueLanguage.INSTANCE) {
    override fun indexStub(stub: XmlAttributeStubImpl, sink: IndexSink) {
      stub.value?.let { sink.occurrence(VueIdIndex.KEY, it) }
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
      return (node?.psi as? XmlAttribute)?.parent
        ?.getAttribute("type")?.value == "text/x-template"
    }
  }

  val SRC_ATTRIBUTE = object : XmlStubBasedAttributeElementType("SRC_ATTRIBUTE", VueLanguage.INSTANCE) {
    override fun indexStub(stub: XmlAttributeStubImpl, sink: IndexSink) {
      stub.value
        ?.let { PathUtil.getFileName(it) }
        ?.takeIf { it.isNotBlank() }
        ?.let { sink.occurrence(VueUrlIndex.KEY, it) }
    }
  }

  val VUE_STUBBED_ATTRIBUTE = object : XmlStubBasedAttributeElementType("VUE_STUBBED_ATTRIBUTE", VueLanguage.INSTANCE) {
  }

}
