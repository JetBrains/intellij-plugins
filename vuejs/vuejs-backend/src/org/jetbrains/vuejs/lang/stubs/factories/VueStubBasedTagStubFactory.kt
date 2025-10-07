// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.stubs.factories

import com.intellij.lang.ASTNode
import com.intellij.lang.stubs.HtmlStubBasedTagStubFactory
import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil.SLOT_TAG_NAME

internal open class VueStubBasedTagStubFactory(elementTypeSupplier: () -> IElementType) : HtmlStubBasedTagStubFactory(elementTypeSupplier) {
  override fun shouldCreateStub(node: ASTNode): Boolean =
    (node.psi as? XmlTag)
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