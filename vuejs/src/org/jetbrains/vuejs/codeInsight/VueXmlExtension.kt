// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSEmbeddedContent
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.xml.SchemaPrefix
import com.intellij.psi.impl.source.xml.TagNameReference
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.xml.HtmlXmlExtension
import org.jetbrains.vuejs.codeInsight.refs.VueTagNameReference
import org.jetbrains.vuejs.codeInsight.tags.VueElementDescriptor
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.model.source.VueComponentDetailsProvider

class VueXmlExtension : HtmlXmlExtension() {
  override fun isAvailable(file: PsiFile?): Boolean = file?.language is VueLanguage

  override fun getPrefixDeclaration(context: XmlTag, namespacePrefix: String?): SchemaPrefix? {
    if (namespacePrefix != null && (namespacePrefix == "v-bind" || namespacePrefix == "v-on" || namespacePrefix.startsWith("@"))) {
      val schemaPrefix = findAttributeSchema(context, namespacePrefix, 0)
      if (schemaPrefix != null) return schemaPrefix
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

    val toAssetName = toAsset(attrName)
    val fromAssetName = fromAsset(attrName)

    return tag?.attributes?.find {
      if (it.name == "v-bind") {
        val jsEmbeddedContent = PsiTreeUtil.findChildOfType(it.valueElement, JSEmbeddedContent::class.java)
        val child = jsEmbeddedContent?.firstChild
        if (child is JSReferenceExpression && child.nextSibling == null) {
          val resolve = child.resolve()
          (resolve as? JSProperty)?.objectLiteralExpressionInitializer?.properties?.forEach { property ->
            if (property.name == toAssetName) return@find true
          }
        }
        return@find false
      }

      return@find fromAsset(VueComponentDetailsProvider.getBoundName(it.name) ?: it.name) == fromAssetName
    } != null
  }

  override fun isCollapsibleTag(tag: XmlTag?): Boolean = false
  override fun isSelfClosingTagAllowed(tag: XmlTag): Boolean = tag.descriptor is VueElementDescriptor
  override fun isSingleTagException(tag: XmlTag): Boolean = tag.descriptor is VueElementDescriptor

  override fun createTagNameReference(nameElement: ASTNode?, startTagFlag: Boolean): TagNameReference? {
    val parentTag = nameElement?.treeParent as? XmlTag
    if (parentTag?.descriptor is VueElementDescriptor) {
      return VueTagNameReference(nameElement, startTagFlag)
    }
    return super.createTagNameReference(nameElement, startTagFlag)
  }
}
