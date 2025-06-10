// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html

import com.intellij.lang.ASTNode
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.PsiBuilderFactory
import com.intellij.psi.ParsingDiagnostics
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.SharedImplUtil
import com.intellij.psi.tree.IFileElementType
import org.jetbrains.vuejs.lang.VueScriptLangs
import org.jetbrains.vuejs.lang.html.lexer.VueParsingLexer
import org.jetbrains.vuejs.lang.html.parser.VueParserDefinition
import org.jetbrains.vuejs.lang.html.parser.VueParsing

class VueFileElementType : IFileElementType("vue", VueLanguage.INSTANCE) {
  companion object {
    @JvmStatic
    val INSTANCE: VueFileElementType = VueFileElementType()

    const val INJECTED_FILE_SUFFIX = ".#@injected@#.html"

    fun readDelimiters(fileName: String?): Pair<String, String>? {
      if (fileName == null || !fileName.endsWith(INJECTED_FILE_SUFFIX)) return null
      val endDot = fileName.length - INJECTED_FILE_SUFFIX.length
      val separatorDot = fileName.lastIndexOf('.', endDot - 1)
      val startDot = fileName.lastIndexOf('.', separatorDot - 1)
      if (endDot < 0 || startDot < 0 || separatorDot < 0) {
        return null
      }
      return Pair(fileName.substring(startDot + 1, separatorDot), fileName.substring(separatorDot + 1, endDot))
    }
  }

  override fun toString(): String {
    return "$language:${super.toString()}"
  }

  override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode {
    val delimiters = readDelimiters(SharedImplUtil.getContainingFile(chameleon).name)
    val languageForParser = getLanguageForParser(psi)
    // TODO support for custom delimiters - port to Angular and merge
    if (languageForParser === VueLanguage.INSTANCE) {
      val project = psi.project
      val htmlCompatMode = !psi.containingFile.isVueFile
      val lexer = VueParserDefinition.Util.createLexer(project, delimiters, htmlCompatMode)
      val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, languageForParser, chameleon.chars)
      val startTime = System.nanoTime()
      lexer as VueParsingLexer
      builder.putUserData(VueScriptLangs.LANG_MODE, lexer.lexedLangMode) // read in VueParsing
      builder.putUserData(VueParsing.HTML_COMPAT_MODE, htmlCompatMode)
      psi.putUserData(VueScriptLangs.LANG_MODE, lexer.lexedLangMode) // read in VueElementTypes
      val parser = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser)!!.createParser(project)
      val node = parser.parse(this, builder)
      ParsingDiagnostics.registerParse(builder, language, System.nanoTime() - startTime)
      return node.firstChildNode
    }

    return super.doParseContents(chameleon, psi)
  }
}
