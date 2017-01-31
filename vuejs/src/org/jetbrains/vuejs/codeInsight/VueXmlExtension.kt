package org.jetbrains.vuejs.codeInsight

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.xml.SchemaPrefix
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.HtmlXmlExtension
import org.jetbrains.vuejs.VueLanguage

class VueXmlExtension : HtmlXmlExtension() {
  override fun isAvailable(file: PsiFile?): Boolean {
    return file?.language is VueLanguage
  }

  override fun getPrefixDeclaration(context: XmlTag, namespacePrefix: String?): SchemaPrefix? {
    if ("v-bind" == namespacePrefix || "v-on" == namespacePrefix) {
      val attribute = findAttributeSchema(context, namespacePrefix, 0)
      if (attribute != null) return attribute
    }
    return super.getPrefixDeclaration(context, namespacePrefix)
  }

  private fun findAttributeSchema(context: XmlTag, namespacePrefix: String, offset: Int): SchemaPrefix? {
    context.attributes
      .filter { it.name.startsWith(namespacePrefix) }
      .forEach { return SchemaPrefix(it, TextRange.create(offset, namespacePrefix.length), namespacePrefix.substring(offset)) }
    return null
  }
}