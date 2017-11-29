package org.jetbrains.vuejs.codeInsight

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.xml.SchemaPrefix
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.HtmlXmlExtension
import org.jetbrains.vuejs.VueLanguage

class VueXmlExtension : HtmlXmlExtension() {
  override fun isAvailable(file: PsiFile?): Boolean = file?.language is VueLanguage

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

  override fun isRequiredAttributeImplicitlyPresent(tag: XmlTag?, attrName: String?): Boolean {
    if (attrName == null) return false
    val otherCase = if (attrName.contains('-')) toAsset(attrName) else fromAsset(attrName)
    val predicate: (XmlAttribute) -> Boolean
    if (otherCase == attrName) {
      predicate = { it.name == "v-bind:" + attrName || it.name == ":" + attrName || it.name == attrName}
    } else {
      predicate = { it.name == "v-bind:" + attrName || it.name == ":" + attrName || it.name == attrName ||
                    it.name == "v-bind:" + otherCase || it.name == ":" + otherCase || it.name == otherCase }
    }
    tag?.attributes?.filter(predicate)?.forEach { return true }
    return super.isRequiredAttributeImplicitlyPresent(tag, attrName)
  }

  override fun isCollapsibleTag(tag: XmlTag?): Boolean = VueTagProvider().getDescriptor(tag) != null
}