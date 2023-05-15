// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes

import com.intellij.html.impl.providers.HtmlAttributeValueProvider
import com.intellij.javascript.web.css.CssClassInJSLiteralOrIdentifierReferenceProvider.Companion.getClassesFromEmbeddedContent
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import com.intellij.xml.util.HtmlUtil
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo
import org.angular2.lang.html.parser.Angular2AttributeNameParser.PropertyBindingInfo
import org.angular2.lang.html.parser.Angular2AttributeNameParser.parse
import org.angular2.lang.html.parser.Angular2AttributeType
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding
import org.angular2.lang.html.psi.PropertyBindingType
import org.angular2.lang.html.psi.PropertyBindingType.CLASS
import org.jetbrains.annotations.NonNls

class Angular2AttributeValueProvider : HtmlAttributeValueProvider() {

  override fun getCustomAttributeValues(tag: XmlTag, attributeName: String): String? {
    if (attributeName.equals(HtmlUtil.CLASS_ATTRIBUTE_NAME, ignoreCase = true) && Angular2LangUtil.isAngular2Context(tag)) {
      val result = SmartList<String>()
      var classAttr: String? = null
      for (attribute in tag.attributes) {
        val attrName = attribute.name
        if (HtmlUtil.CLASS_ATTRIBUTE_NAME.equals(attrName, ignoreCase = true)) {
          classAttr = attribute.value
        }
        else {
          getCustomAttributeValues(tag, attrName)?.let { result.add(it) }
        }
      }
      if (!result.isEmpty()) {
        classAttr?.let { result.add(it) }
        return StringUtil.join(result, " ")
      }
      return null
    }
    else {
      val info = parse(attributeName, tag)
      if (isNgClassAttribute(info)) {
        val attribute = tag.getAttribute(attributeName)
        if (attribute is Angular2HtmlPropertyBinding) {
          return getClassesFromEmbeddedContent(attribute.binding)
        }
      }
      else if (info is PropertyBindingInfo
               && info.bindingType == CLASS
               && Angular2LangUtil.isAngular2Context(tag)) {
        return info.name
      }
    }
    return null
  }

  override fun getCustomAttributeValue(tag: XmlTag, attributeName: String): PsiElement? {
    if (attributeName == SRC_ATTR
        && tag.localName.equals(IMG_TAG, true)
        && Angular2LangUtil.isAngular2Context(tag)) {
      for (attribute in tag.attributes) {
        val attrName = attribute.name
        val info = parse(attrName, tag)
        if (isNgSrcAttribute(info)) {
          return attribute.valueElement
        }
      }
    }
    return null
  }

  companion object {

    @NonNls
    val NG_CLASS_ATTR = "ngClass"

    @NonNls
    val SRC_ATTR = "src"

    @NonNls
    val IMG_TAG = "img"

    @NonNls
    val NG_SRC_ATTR = "ngSrc"

    fun isNgClassAttribute(attribute: XmlAttribute?): Boolean {
      return attribute != null && isNgClassAttribute(parse(attribute.name, attribute.parent))
    }

    fun isNgClassAttribute(info: AttributeInfo): Boolean {
      return (info is PropertyBindingInfo
              && info.bindingType == PropertyBindingType.PROPERTY
              && NG_CLASS_ATTR == info.name)
    }

    fun isNgSrcAttribute(info: AttributeInfo): Boolean {
      return NG_SRC_ATTR == info.name &&
             (info.asSafely<PropertyBindingInfo>()?.bindingType == PropertyBindingType.PROPERTY
             || info.type == Angular2AttributeType.REGULAR)
    }
  }

}
