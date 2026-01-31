// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2

import com.intellij.javascript.web.WebFramework
import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.html.attributes.HtmlAttributeSymbolDescriptor
import com.intellij.polySymbols.html.attributes.HtmlAttributeSymbolInfo
import com.intellij.polySymbols.html.elements.HtmlElementSymbolDescriptor
import com.intellij.polySymbols.html.elements.HtmlElementSymbolInfo
import com.intellij.polySymbols.query.PolySymbolNamesProvider
import com.intellij.polySymbols.query.PolySymbolNamesProvider.Target.NAMES_QUERY
import com.intellij.polySymbols.query.PolySymbolNamesProvider.Target.RENAME_QUERY
import com.intellij.psi.xml.XmlTag
import icons.AngularIcons
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor
import org.angular2.codeInsight.tags.Angular2ElementDescriptor
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.html.Angular17HtmlFileType
import org.angular2.lang.html.Angular181HtmlFileType
import org.angular2.lang.html.Angular20HtmlFileType
import org.angular2.lang.html.Angular2HtmlDialect
import org.angular2.lang.html.Angular2HtmlFileType
import org.angular2.lang.html.Angular2TemplateSyntax
import org.angular2.lang.svg.Angular17SvgFileType
import org.angular2.lang.svg.Angular181SvgFileType
import org.angular2.lang.svg.Angular20SvgFileType
import org.angular2.lang.svg.Angular2SvgFileType
import org.angular2.web.Angular2AttributeNameCodeCompletionFilter
import org.angular2.web.NG_DIRECTIVE_IN_OUTS
import javax.swing.Icon

class Angular2Framework : WebFramework() {

  override val icon: Icon
    get() = AngularIcons.Angular2

  override val displayName: String
    get() = "Angular"

  override fun getFileType(kind: SourceFileKind, context: VirtualFile, project: Project): WebFrameworkHtmlFileType? =
    when (kind) {
      SourceFileKind.HTML -> when (Angular2LangUtil.getTemplateSyntax(project, context)) {
        Angular2TemplateSyntax.V_2, Angular2TemplateSyntax.V_2_NO_EXPANSION_FORMS -> Angular2HtmlFileType
        Angular2TemplateSyntax.V_17 -> Angular17HtmlFileType
        Angular2TemplateSyntax.V_18_1 -> Angular181HtmlFileType
        Angular2TemplateSyntax.V_20 -> Angular20HtmlFileType
      }
      SourceFileKind.SVG -> when (Angular2LangUtil.getTemplateSyntax(project, context)) {
        Angular2TemplateSyntax.V_2, Angular2TemplateSyntax.V_2_NO_EXPANSION_FORMS -> Angular2SvgFileType
        Angular2TemplateSyntax.V_17 -> Angular17SvgFileType
        Angular2TemplateSyntax.V_18_1 -> Angular181SvgFileType
        Angular2TemplateSyntax.V_20 -> Angular20SvgFileType
      }
      else -> null
    }

  override fun isOwnTemplateLanguage(language: Language): Boolean =
    language is Angular2HtmlDialect

  override fun createHtmlAttributeDescriptor(
    info: HtmlAttributeSymbolInfo,
    tag: XmlTag?,
  ): HtmlAttributeSymbolDescriptor =
    Angular2AttributeDescriptor(info, tag)

  override fun createHtmlElementDescriptor(
    info: HtmlElementSymbolInfo,
    tag: XmlTag,
  ): HtmlElementSymbolDescriptor =
    Angular2ElementDescriptor(info, tag)

  override fun getAttributeNameCodeCompletionFilter(tag: XmlTag): Angular2AttributeNameCodeCompletionFilter =
    Angular2AttributeNameCodeCompletionFilter(tag)

  override fun getNames(
    qualifiedName: PolySymbolQualifiedName,
    target: PolySymbolNamesProvider.Target,
  ): List<String> {
    if ((target == NAMES_QUERY || target == RENAME_QUERY)
        && qualifiedName.kind == NG_DIRECTIVE_IN_OUTS) {
      // Required to find usages and rename for model signal
      return listOf(
        qualifiedName.name,
        qualifiedName.name + Angular2LangUtil.OUTPUT_CHANGE_SUFFIX
      )
    }
    return super.getNames(qualifiedName, target)
  }

  companion object {

    const val ID: String = "angular"

  }
}

val angular2Framework: WebFramework
  get() = WebFramework.get(Angular2Framework.ID)