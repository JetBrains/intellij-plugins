// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html

import com.intellij.javascript.web.html.WebFrameworkHtmlFileType
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.xml.impl.XmlBraceMatcher
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes

class Angular17HtmlBraceMatcher : XmlBraceMatcher() {

  override fun getPairedBraceMatcher(tokenType: IElementType?): PairedBraceMatcher =
    super.getPairedBraceMatcher(tokenType) ?: AngularBlocksBraceMatcher

  override fun isFileTypeWithSingleHtmlTags(fileType: FileType?): Boolean =
    fileType is WebFrameworkHtmlFileType
    && fileType.dialect.let { it is Angular2HtmlDialect && it.templateSyntax >= Angular2TemplateSyntax.V_17 }

  private object AngularBlocksBraceMatcher : PairedBraceMatcher {

    private val pairs = arrayOf(
      BracePair(Angular2HtmlTokenTypes.BLOCK_START, Angular2HtmlTokenTypes.BLOCK_END, false),
      BracePair(Angular2HtmlTokenTypes.BLOCK_PARAMETERS_START, Angular2HtmlTokenTypes.BLOCK_PARAMETERS_END, false)
    )

    override fun getPairs(): Array<BracePair> = pairs

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean =
      true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int =
      openingBraceOffset

  }

}