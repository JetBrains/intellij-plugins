// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2

import com.intellij.html.webSymbols.attributes.WebSymbolAttributeDescriptor
import com.intellij.html.webSymbols.attributes.WebSymbolHtmlAttributeInfo
import com.intellij.html.webSymbols.elements.WebSymbolElementDescriptor
import com.intellij.html.webSymbols.elements.WebSymbolHtmlElementInfo
import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.lang.html.WebFrameworkHtmlFileType
import com.intellij.psi.xml.XmlTag
import icons.AngularJSIcons
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.tags.Angular2ElementDescriptor
import org.angular2.lang.html.Angular2HtmlFileType
import org.angular2.lang.svg.Angular2SvgFileType
import org.angular2.web.Angular2AttributeNameCodeCompletionFilter
import javax.swing.Icon

class Angular2Framework : WebFramework() {

  override val icon: Icon
    get() = AngularJSIcons.Angular2

  override val displayName: String
    get() = "Angular"

  override val htmlFileType: WebFrameworkHtmlFileType?
    get() = Angular2HtmlFileType.INSTANCE

  override val svgFileType: WebFrameworkHtmlFileType?
    get() = Angular2SvgFileType.INSTANCE

  override fun createHtmlAttributeDescriptor(info: WebSymbolHtmlAttributeInfo,
                                             tag: XmlTag?): WebSymbolAttributeDescriptor =
    Angular2AttributeDescriptor(info, tag)

  override fun createHtmlElementDescriptor(info: WebSymbolHtmlElementInfo,
                                           tag: XmlTag): WebSymbolElementDescriptor =
    Angular2ElementDescriptor(info, tag)

  override fun getAttributeNameCodeCompletionFilter(tag: XmlTag) =
    Angular2AttributeNameCodeCompletionFilter(tag)

  companion object {

    const val ID = "angular"

    @JvmStatic
    val instance: WebFramework
      get() = get(ID)
  }
}
