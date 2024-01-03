// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2

import com.intellij.html.webSymbols.attributes.WebSymbolAttributeDescriptor
import com.intellij.html.webSymbols.attributes.WebSymbolHtmlAttributeInfo
import com.intellij.html.webSymbols.elements.WebSymbolElementDescriptor
import com.intellij.html.webSymbols.elements.WebSymbolHtmlElementInfo
import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.xml.XmlTag
import icons.AngularJSIcons
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.tags.Angular2ElementDescriptor
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.html.Angular17HtmlFileType
import org.angular2.lang.html.Angular2HtmlDialect
import org.angular2.lang.html.Angular2HtmlFileType
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.svg.Angular17SvgFileType
import org.angular2.lang.svg.Angular2SvgFileType
import org.angular2.web.Angular2AttributeNameCodeCompletionFilter
import javax.swing.Icon

class Angular2Framework : WebFramework() {

  override val icon: Icon
    get() = AngularJSIcons.Angular2

  override val displayName: String
    get() = "Angular"

  override fun getFileType(kind: SourceFileKind, context: VirtualFile, project: Project): WebFrameworkHtmlFileType? =
    when (kind) {
      SourceFileKind.HTML -> when (Angular2LangUtil.getTemplateSyntax(project, context)) {
        Angular2TemplateSyntax.V_2, Angular2TemplateSyntax.V_2_NO_EXPANSION_FORMS -> Angular2HtmlFileType.INSTANCE
        Angular2TemplateSyntax.V_17 -> Angular17HtmlFileType.INSTANCE
      }
      SourceFileKind.SVG -> when (Angular2LangUtil.getTemplateSyntax(project, context)) {
        Angular2TemplateSyntax.V_2, Angular2TemplateSyntax.V_2_NO_EXPANSION_FORMS -> Angular2SvgFileType.INSTANCE
        Angular2TemplateSyntax.V_17 -> Angular17SvgFileType.INSTANCE
      }
      else -> null
    }

  override fun isOwnTemplateLanguage(language: Language): Boolean =
    language is Angular2HtmlDialect

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
