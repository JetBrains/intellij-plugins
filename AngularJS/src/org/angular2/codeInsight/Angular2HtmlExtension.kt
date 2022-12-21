// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.html.webSymbols.WebSymbolsHtmlQueryConfigurator
import com.intellij.html.webSymbols.WebSymbolsXmlExtension
import com.intellij.html.webSymbols.elements.WebSymbolElementDescriptor
import com.intellij.javascript.web.WebFramework
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.SmartList
import com.intellij.util.io.URLUtil
import com.intellij.webSymbols.utils.unwrapMatchedSymbols
import com.intellij.xml.util.XmlUtil
import org.angular2.Angular2Framework
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.html.psi.Angular2HtmlBananaBoxBinding
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding
import org.angular2.lang.html.psi.PropertyBindingType
import org.angular2.lang.svg.Angular2SvgLanguage

class Angular2HtmlExtension : WebSymbolsXmlExtension() {

  override fun isAvailable(file: PsiFile?): Boolean {
    return (file != null
            && WebFramework.forFileType(file.fileType) == Angular2Framework.instance
            && Angular2LangUtil.isAngular2Context(file))
  }

  override fun isSelfClosingTagAllowed(tag: XmlTag): Boolean {
    if (tag.language.`is`(Angular2SvgLanguage.INSTANCE)) return true
    val descriptor = tag.descriptor
    if (descriptor is WebSymbolElementDescriptor) {
      val hasStandardSymbol = descriptor.symbol
        .unwrapMatchedSymbols()
        .any { it is WebSymbolsHtmlQueryConfigurator.StandardHtmlSymbol }
      if (!hasStandardSymbol) return true
    }
    return super.isSelfClosingTagAllowed(tag)
  }

  override fun isRequiredAttributeImplicitlyPresent(tag: XmlTag?, attrName: String?): Boolean {
    if (tag == null || attrName == null) return false
    var result: Boolean? = null
    tag.acceptChildren(object : Angular2HtmlElementVisitor() {
      override fun visitPropertyBinding(propertyBinding: Angular2HtmlPropertyBinding) {
        checkBinding(propertyBinding.bindingType, propertyBinding.propertyName)
      }

      override fun visitBananaBoxBinding(bananaBoxBinding: Angular2HtmlBananaBoxBinding) {
        checkBinding(bananaBoxBinding.bindingType, bananaBoxBinding.propertyName)
      }

      private fun checkBinding(type: PropertyBindingType,
                               name: String) {
        if ((type == PropertyBindingType.PROPERTY || type == PropertyBindingType.ATTRIBUTE) && attrName == name) {
          result = true
        }
      }
    })
    return result ?: super.isRequiredAttributeImplicitlyPresent(tag, attrName)
  }

  override fun getCharEntitiesDTDs(file: XmlFile): List<XmlFile> {
    val result = SmartList(super.getCharEntitiesDTDs(file))
    XmlUtil.findXmlFile(file, NG_ENT_LOCATION.value)?.let { result.add(it) }
    return result
  }

  companion object {
    private val NG_ENT_LOCATION = NotNullLazyValue.lazy {
      val url = Angular2HtmlExtension::class.java.getResource("/dtd/ngChars.ent")
      VfsUtilCore.urlToPath(VfsUtilCore.fixURLforIDEA(
        URLUtil.unescapePercentSequences(url!!.toExternalForm())))
    }
  }
}
