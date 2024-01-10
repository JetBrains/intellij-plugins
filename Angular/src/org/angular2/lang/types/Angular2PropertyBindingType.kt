// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types

import com.intellij.lang.javascript.psi.JSExpectedTypeKind
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.psi.xml.XmlAttribute
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.html.parser.Angular2AttributeNameParser.PropertyBindingInfo
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.PropertyBindingType

class Angular2PropertyBindingType : Angular2BaseType<XmlAttribute> {
  constructor(attribute: XmlAttribute, expectedKind: JSExpectedTypeKind? = null) : super(attribute, XmlAttribute::class.java) {
    this.expectedKind = expectedKind
  }

  private constructor(source: JSTypeSource, expectedKind: JSExpectedTypeKind?) : super(source, XmlAttribute::class.java) {
    this.expectedKind = expectedKind
  }

  private val expectedKind: JSExpectedTypeKind?

  override val typeOfText: String
    get() = sourceElement.name

  override fun copyWithNewSource(source: JSTypeSource): JSType {
    return Angular2PropertyBindingType(source, expectedKind)
  }

  override fun resolveType(context: JSTypeSubstitutionContext): JSType? {
    return BindingsTypeResolver.resolve(sourceElement,
                                        expectedKind,
                                        { isPropertyBindingAttribute(it) },
                                        BindingsTypeResolver::resolveDirectiveInputType)
  }

  companion object {
    fun isPropertyBindingAttribute(info: Angular2AttributeNameParser.AttributeInfo): Boolean {
      return (info.type == Angular2AttributeType.BANANA_BOX_BINDING
              || (info.type == Angular2AttributeType.PROPERTY_BINDING
                  && (info as PropertyBindingInfo).bindingType == PropertyBindingType.PROPERTY))
    }
  }
}