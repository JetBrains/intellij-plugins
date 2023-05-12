// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.html.webSymbols.WebSymbolsXmlExtension
import com.intellij.html.webSymbols.elements.WebSymbolElementDescriptor
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType.Companion.isDotVueFile
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueModelDirectiveProperties
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.web.getModel

class VueXmlExtension : WebSymbolsXmlExtension() {
  override fun isAvailable(file: PsiFile?): Boolean =
    file?.let {
      it.language is VueLanguage
      // Support extension in plain HTML with Vue.js lib, PHP, Twig and others
      || (HTMLLanguage.INSTANCE in it.viewProvider.languages && isVueContext(it))
    } == true

  override fun isRequiredAttributeImplicitlyPresent(tag: XmlTag?, attrName: String?): Boolean {
    if (attrName == null) return false

    val toAssetName = toAsset(attrName)
    val fromAssetName = fromAsset(attrName)

    return tag?.attributes?.find { attr ->
      if (attr.name == "v-bind") {
        return@find attr.valueElement
          ?.findJSExpression<JSExpression>()
          ?.let { JSResolveUtil.getElementJSType(it) }
          ?.asRecordType()
          ?.findPropertySignature(toAssetName) != null
      }
      val info = VueAttributeNameParser.parse(attr.name, tag)
      var name: String? = null
      if (info is VueAttributeNameParser.VueDirectiveInfo) {
        if (info.directiveKind == VueAttributeNameParser.VueDirectiveKind.MODEL) {
          name = info.arguments
                 ?: (tag.descriptor as? WebSymbolElementDescriptor)?.getModel()?.prop
                 ?: VueModelDirectiveProperties.getDefault(tag).prop
        }
        else if (info.directiveKind === VueAttributeNameParser.VueDirectiveKind.BIND
                 && info.arguments != null) {
          name = info.arguments
        }
      }
      return@find fromAsset(name ?: info.name) == fromAssetName
    } != null
  }

  override fun isSelfClosingTagAllowed(tag: XmlTag): Boolean =
    isVueComponentTemplateContext(tag)
    || super.isSelfClosingTagAllowed(tag)

  private fun isVueComponentTemplateContext(tag: XmlTag) =
    tag.containingFile.let {
      it.isDotVueFile || VueModelManager.findEnclosingContainer(it) is VueComponent
    }

}
