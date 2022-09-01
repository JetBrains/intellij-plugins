// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.psi.impl.source.html.HtmlStubBasedTagElementType
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.model.SLOT_TAG_NAME

open class VueStubBasedTagElementType(debugName: String) : HtmlStubBasedTagElementType(debugName, VueLanguage.INSTANCE) {
  override fun shouldCreateStub(node: ASTNode?): Boolean =
    (node?.psi as? XmlTag)
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