// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.parser

import com.intellij.html.embedding.HtmlCustomEmbeddedContentTokenType
import com.intellij.ide.highlighter.custom.AbstractCustomLexer
import com.intellij.ide.highlighter.custom.tokens.TokenParser
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSStubElementType
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.stubs.JSVariableStub
import com.intellij.lexer.Lexer
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.containers.ContainerUtil
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.html.XmlASTWrapperPsiElement
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes
import java.util.function.Supplier

class Angular2HtmlVarAttrTokenType(debugName: String,
                                   private val myVarElementType: JSStubElementType<JSVariableStub<JSVariable>, JSVariable>,
                                   private val myPrefixTokenParserConstructor: Supplier<out TokenParser>)
  : HtmlCustomEmbeddedContentTokenType(debugName, Angular2Language.INSTANCE) {
  override fun createLexer(): Lexer {
    return AbstractCustomLexer(ContainerUtil.newArrayList(
      myPrefixTokenParserConstructor.get(), VarIdentTokenParser()))
  }

  override fun parse(builder: PsiBuilder) {
    assert(builder.tokenType === XmlTokenType.XML_NAME)
    val start = builder.mark()
    builder.advanceLexer()
    val `var` = builder.mark()
    builder.advanceLexer()
    `var`.done(myVarElementType)
    `var`.precede().done(JSStubElementTypes.VAR_STATEMENT)
    start.done(XmlTokenType.XML_NAME)
  }

  override fun createPsi(node: ASTNode): PsiElement {
    return XmlASTWrapperPsiElement(node)
  }

  private class VarIdentTokenParser : TokenParser() {
    override fun hasToken(position: Int): Boolean {
      if (position == myStartOffset) {
        return false
      }
      myTokenInfo.updateData(position, myEndOffset, JSTokenTypes.IDENTIFIER)
      return true
    }
  }

  private class RefPrefixTokenParser : TokenParser() {
    override fun hasToken(position: Int): Boolean {
      if (position == myStartOffset) {
        myTokenInfo.updateData(position, position + if (myBuffer[0] == '#') 1 else 4, XmlTokenType.XML_NAME)
        return true
      }
      return false
    }
  }

  private class LetPrefixTokenParser : TokenParser() {
    override fun hasToken(position: Int): Boolean {
      if (position == myStartOffset) {
        myTokenInfo.updateData(position, position + 4, XmlTokenType.XML_NAME)
        return true
      }
      return false
    }
  }

  companion object {
    @JvmField
    val REFERENCE = Angular2HtmlVarAttrTokenType("NG:REFERENCE_TOKEN", Angular2HtmlStubElementTypes.REFERENCE_VARIABLE) {
      RefPrefixTokenParser()
    }

    @JvmField
    val LET = Angular2HtmlVarAttrTokenType("NG:LET_TOKEN", Angular2HtmlStubElementTypes.LET_VARIABLE) {
      LetPrefixTokenParser()
    }
  }
}