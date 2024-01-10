// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser

import com.intellij.psi.tree.IElementType
import com.intellij.psi.xml.XmlElementType
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes

enum class Angular2AttributeType(val elementType: IElementType,
                                 private val myPrefix: String,
                                 private val mySuffix: String,
                                 private val canonicalPrefix: String?) {
  REFERENCE(Angular2HtmlElementTypes.REFERENCE, "#", "", "ref-"),
  REGULAR(XmlElementType.XML_ATTRIBUTE, "", "", null),
  LET(Angular2HtmlElementTypes.LET, "let-", "", null),
  BANANA_BOX_BINDING(Angular2HtmlElementTypes.BANANA_BOX_BINDING, "[(", ")]", "bindon-"),
  PROPERTY_BINDING(Angular2HtmlElementTypes.PROPERTY_BINDING, "[", "]", "bind-"),
  EVENT(Angular2HtmlElementTypes.EVENT, "(", ")", "on-"),
  TEMPLATE_BINDINGS(Angular2HtmlElementTypes.TEMPLATE_BINDINGS, "*", "", null),
  NG_CONTENT_SELECTOR(Angular2HtmlStubElementTypes.NG_CONTENT_SELECTOR, "", "", null),
  I18N(XmlElementType.XML_ATTRIBUTE, "i18n-", "", null);

  fun buildName(name: String): String {
    return buildName(name, false)!!
  }

  fun buildName(name: String, canonical: Boolean): String? {
    return if (canonical) {
      if (canonicalPrefix == null) {
        null
      }
      else canonicalPrefix + name
    }
    else myPrefix + name + mySuffix
  }
}