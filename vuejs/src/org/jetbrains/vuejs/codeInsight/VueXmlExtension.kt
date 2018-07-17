// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.xml.SchemaPrefix
import com.intellij.psi.impl.source.xml.TagNameReference
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
    val normalized = fromAsset(attrName)
    if (tag?.attributes?.filter {
        val extractedName = VueComponentDetailsProvider.getBoundName(it.name) ?: it.name
        return@filter normalized == fromAsset(extractedName)
      }?.any() == true) return true
    return super.isRequiredAttributeImplicitlyPresent(tag, attrName)
  }

  override fun isCollapsibleTag(tag: XmlTag?): Boolean = false
  override fun isSelfClosingTagAllowed(tag: XmlTag): Boolean = VueTagProvider().getDescriptor(tag) != null
  override fun isSingleTagException(tag: XmlTag): Boolean = tag.descriptor is VueElementDescriptor

  override fun createTagNameReference(nameElement: ASTNode?, startTagFlag: Boolean): TagNameReference? {
    val parentTag = nameElement?.treeParent as? XmlTag
    if (parentTag != null) {
      val descriptor = VueTagProvider().getDescriptor(parentTag)
      if (descriptor != null) {
        return VueTagNameReference(nameElement, startTagFlag)
      }
    }
    return super.createTagNameReference(nameElement, startTagFlag)
  }
}