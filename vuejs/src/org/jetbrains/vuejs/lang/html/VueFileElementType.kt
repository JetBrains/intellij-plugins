// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html

import com.intellij.lang.ASTNode
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.PsiBuilderFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.StubBuilder
import com.intellij.psi.impl.source.tree.SharedImplUtil
import com.intellij.psi.stubs.DefaultStubBuilder
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.psi.xml.HtmlFileElementType
import org.jetbrains.vuejs.index.VUE_FILE_EXTENSION
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.VueScriptLangs
import org.jetbrains.vuejs.lang.expr.parser.VueJSStubElementTypes
import org.jetbrains.vuejs.lang.html.lexer.VueParsingLexer
import org.jetbrains.vuejs.lang.html.parser.VueParserDefinition
import org.jetbrains.vuejs.lang.html.parser.VueParsing
import org.jetbrains.vuejs.lang.html.parser.VueStubElementTypes
import org.jetbrains.vuejs.lang.html.stub.impl.VueFileStubImpl

class VueFileElementType : IStubFileElementType<VueFileStubImpl>("vue", VueLanguage.INSTANCE) {
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

  override fun getExternalId(): String {
    return "$language:$this"
  }

  override fun getBuilder(): StubBuilder {
    return object : DefaultStubBuilder() {
      override fun createStubForFile(file: PsiFile): StubElement<*> {
        return if (file is VueFile) VueFileStubImpl(file) else super.createStubForFile(file)
      }
    }
  }

  override fun serialize(stub: VueFileStubImpl, dataStream: StubOutputStream) {
    dataStream.writeName(stub.langMode.canonicalAttrValue)
  }

  override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): VueFileStubImpl {
    return VueFileStubImpl(LangMode.fromAttrValue(dataStream.readNameString()!!))
  }

  override fun doParseContents(chameleon: ASTNode, psi: PsiElement): ASTNode {
    val delimiters = readDelimiters(SharedImplUtil.getContainingFile(chameleon).name)
    val languageForParser = getLanguageForParser(psi)
    // TODO support for custom delimiters - port to Angular and merge
    if (languageForParser === VueLanguage.INSTANCE) {
      val project = psi.project
      val lexer = VueParserDefinition.createLexer(project, delimiters)
      val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, languageForParser, chameleon.chars)
      lexer as VueParsingLexer
      builder.putUserData(VueScriptLangs.LANG_MODE, lexer.lexedLangMode) // read in VueParsing
      builder.putUserData(VueParsing.HTML_COMPAT_MODE, !psi.containingFile.name.endsWith(VUE_FILE_EXTENSION))
      psi.putUserData(VueScriptLangs.LANG_MODE, lexer.lexedLangMode) // read in VueElementTypes
      val parser = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser)!!.createParser(project)
      val node = parser.parse(this, builder)

      return node.firstChildNode
    }

    return super.doParseContents(chameleon, psi)
  }
}
