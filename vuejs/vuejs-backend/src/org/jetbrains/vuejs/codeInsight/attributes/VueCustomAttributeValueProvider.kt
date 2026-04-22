// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.attributes

import com.intellij.html.impl.providers.HtmlAttributeValueProvider
import com.intellij.javascript.backend.css.polySymbols.CssClassListInJSLiteralInHtmlAttributeScope.Companion.getClassesFromEmbeddedContent
import com.intellij.xml.util.getCustomHtmlClassAttributeValue
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.util.HtmlUtil
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.Companion.parse
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent

//TODO migrate to use symbols
class VueCustomAttributeValueProvider : HtmlAttributeValueProvider() {

  override fun getCustomAttributeValues(tag: XmlTag, attributeName: String): String? {
    if (attributeName.equals(HtmlUtil.CLASS_ATTRIBUTE_NAME, ignoreCase = true)
        && isVueContext(tag)) {
      return getCustomHtmlClassAttributeValue(tag) { attribute ->
        if (HtmlUtil.CLASS_ATTRIBUTE_NAME.equals(attribute.name, ignoreCase = true)) null
        else getCustomAttributeValues(tag, attribute.name)
      }
    }
    else if (isVBindClassAttribute(parse(attributeName, tag))) {
      return getClassNames(tag.getAttribute(attributeName))
    }
    return null
  }

}

fun isVBindClassAttribute(attribute: XmlAttribute?): Boolean =
  attribute
    ?.let { parse(it.name, it.parent) }
    .let { isVBindClassAttribute(it) }

fun isVBindClassAttribute(info: VueAttributeNameParser.VueAttributeInfo?): Boolean =
  info is VueAttributeNameParser.VueDirectiveInfo
  && info.directiveKind == VueAttributeNameParser.VueDirectiveKind.BIND
  && info.arguments == HtmlUtil.CLASS_ATTRIBUTE_NAME

private fun getClassNames(attribute: XmlAttribute?): String {
  return getClassesFromEmbeddedContent(PsiTreeUtil.findChildOfType(attribute, VueJSEmbeddedExpressionContent::class.java))
}