// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.html.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.PsiBuilderFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.impl.source.tree.SharedImplUtil
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.psi.xml.HtmlFileElementType
import org.jetbrains.vuejs.lang.expr.parser.VueJSStubElementTypes
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueFileElementType : IStubFileElementType<PsiFileStub<HtmlFileImpl>>("vue.file", VueLanguage.INSTANCE) {
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

  override fun getStubVersion(): Int {
    return HtmlFileElementType.getHtmlStubVersion() + VueStubElementTypes.VERSION + VueJSStubElementTypes.STUB_VERSION
  }

  override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode {
    val delimiters = readDelimiters(SharedImplUtil.getContainingFile(chameleon).name)
    val languageForParser = getLanguageForParser(psi)
    // TODO support for custom delimiters - port to Angular and merge
    if (delimiters != null
        && languageForParser === VueLanguage.INSTANCE) {
      val project = psi.project
      val builder = PsiBuilderFactory.getInstance().createBuilder(
        project, chameleon, VueParserDefinition.createLexer(project, delimiters), languageForParser, chameleon.chars)
      val parser = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser)!!.createParser(project)
      val node = parser.parse(this, builder)
      return node.firstChildNode
    }
    return super.doParseContents(chameleon, psi)
  }
}
