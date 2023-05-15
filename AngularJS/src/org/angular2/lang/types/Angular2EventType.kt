// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types

import com.intellij.javascript.webSymbols.jsType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.containers.ContainerUtil
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlEvent

class Angular2EventType : Angular2BaseType<XmlAttribute> {
  constructor(attribute: XmlAttribute) : super(attribute, XmlAttribute::class.java)
  private constructor(source: JSTypeSource) : super(source, XmlAttribute::class.java)

  override val typeOfText: String
    get() = "eventof#" + sourceElement.name

  override fun copyWithNewSource(source: JSTypeSource): JSType {
    return Angular2EventType(source)
  }

  override fun resolveType(context: JSTypeSubstitutionContext): JSType? {
    val attribute = sourceElement
    val descriptor = (attribute.descriptor as? Angular2AttributeDescriptor)
    return if (descriptor != null && ContainerUtil.isEmpty(descriptor.sourceDirectives)) {
      descriptor.symbol.jsType
    }
    else BindingsTypeResolver.resolve(attribute, { isEventAttribute(it) }, { obj, name -> obj.resolveDirectiveEventType(name) })
  }

  companion object {
    private fun isEventAttribute(info: Angular2AttributeNameParser.AttributeInfo): Boolean {
      return (info.type == Angular2AttributeType.EVENT
              && (info as Angular2AttributeNameParser.EventInfo).eventType == Angular2HtmlEvent.EventType.REGULAR)
    }
  }
}