// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.expr.formatter

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.javascript.JSLanguageUtil
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.formatter.JSBlockContext
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.formatter.JSSpacingProcessor
import com.intellij.lang.javascript.formatter.JavascriptFormattingModelBuilder
import com.intellij.lang.javascript.formatter.blocks.CompositeJSBlock
import com.intellij.lang.javascript.formatter.blocks.JSBlock
import com.intellij.lang.javascript.formatter.blocks.SubBlockVisitor
import com.intellij.lang.javascript.formatter.blocks.alignment.ASTNodeBasedAlignmentFactory
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.lang.typescript.formatter.TypedJSSpacingProcessor
import com.intellij.lang.typescript.formatter.blocks.TypedJSSubBlockVisitor
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.WrappingUtil
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import org.angular2.codeInsight.blocks.BLOCK_FOR
import org.angular2.lang.expr.lexer.Angular2TokenTypes
import org.angular2.lang.expr.parser.Angular2ElementTypes
import org.angular2.lang.expr.parser.Angular2StubElementTypes
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.html.psi.Angular2HtmlBlockContents
import org.angular2.lang.html.psi.formatter.Angular2HtmlCodeStyleSettings

class Angular2FormattingModelBuilder : JavascriptFormattingModelBuilder() {
  override fun createModel(formattingContext: FormattingContext): FormattingModel {
    val element = formattingContext.psiElement
    val settings = formattingContext.codeStyleSettings
    val dialect = JSLanguageUtil.getLanguageDialect(element)
    val alignment = element.node.getUserData(BLOCK_ALIGNMENT)
    val jsBlockContext: JSBlockContext = Angular2BlockContext(settings, dialect, formattingContext)
    var rootBlock: Block
    if (element.parent.let { it is XmlTag || it is XmlDocument || it is Angular2HtmlBlockContents }) {
      // interpolations
      val wrapType = WrappingUtil.getWrapType(settings.getCustomSettings(Angular2HtmlCodeStyleSettings::class.java).INTERPOLATION_WRAP)
      rootBlock = jsBlockContext.createBlock(element.node, Wrap.createWrap(wrapType, true),
                                             alignment, Indent.getNormalIndent(), null, null)

      // Try to get a correct file element type
      val fileElementType = LanguageParserDefinitions.INSTANCE.forLanguage(dialect)?.fileNodeType as? JSFileElementType
                            ?: JSFileElementType.getByLanguage(dialect)
                            ?: run {
                              thisLogger().error("No JSFileElementType registered for language $dialect of element $element, using plain JavaScript")
                              JSFileElementType.getByLanguage(JavascriptLanguage)
                            }

      // Wrap with a composite block to add indentation
      rootBlock = CompositeJSBlock(listOf(rootBlock), { _, _ -> null }, null,
                                   fileElementType, jsBlockContext)
    }
    else {
      rootBlock = jsBlockContext.createBlock(element.node, null, alignment, null, null, null)
    }
    return createJSFormattingModel(element.containingFile, settings, rootBlock)
  }

  private class Angular2BlockContext(settings: CodeStyleSettings, dialect: Language, formattingContext: FormattingContext)
    : JSBlockContext(settings, dialect, null, formattingContext.formattingMode) {
    override fun createSpacingProcessor(node: ASTNode?, child1: ASTNode?, child2: ASTNode?): JSSpacingProcessor {
      return Angular2SpacingProcessor(node, child1, child2, topSettings, dialect, dialectSettings)
    }

    override fun indentEachBinaryOperandSeparately(child: ASTNode, parentBlock: JSBlock?): Boolean {
      return super.indentEachBinaryOperandSeparately(child, parentBlock)
             || parentBlock?.node?.elementType == Angular2StubElementTypes.BLOCK_PARAMETER_VARIABLE
    }

    override fun createSubBlockVisitor(parentBlock: JSBlock, alignmentFactory: ASTNodeBasedAlignmentFactory?): SubBlockVisitor {
      return object : TypedJSSubBlockVisitor(parentBlock, alignmentFactory, this) {
        override fun getIndent(node: ASTNode, child: ASTNode, sharedSmartIndent: Indent?): Indent? {
          if (node.elementType == Angular2StubElementTypes.BLOCK_PARAMETER_VARIABLE
              && child.elementType === JSTokenTypes.IDENTIFIER
          ) {
            return Indent.getNoneIndent()
          }
          return super.getIndent(node, child, sharedSmartIndent)
        }
      }
    }
  }

  private class Angular2SpacingProcessor(
    parent: ASTNode?,
    child1: ASTNode?,
    child2: ASTNode?,
    settings: CodeStyleSettings?,
    dialect: Language?,
    dialectSettings: JSCodeStyleSettings?,
  ) : TypedJSSpacingProcessor(parent, child1, child2, settings, dialect, dialectSettings) {

    override fun visitElement(node: ASTNode) {
      when (node.elementType) {
        Angular2ElementTypes.BLOCK_PARAMETER_STATEMENT ->
          visitBlockParameterStatement(node)
        Angular2StubElementTypes.BLOCK_PARAMETER_VARIABLE ->
          visitVariable(node)
        Angular2StubElementTypes.DEFERRED_TIME_LITERAL_EXPRESSION ->
          visitDeferredTimeLiteralExpression()
        else -> super.visitElement(node)
      }
    }

    private fun visitBlockParameterStatement(node: ASTNode) {
      if (myChild1.elementType == Angular2TokenTypes.BLOCK_PARAMETER_NAME
          || myChild1.elementType == Angular2TokenTypes.BLOCK_PARAMETER_PREFIX) {
        setSingleSpace(myChild2 != null && myChild2.elementType != TokenType.ERROR_ELEMENT)
      }
      else if (myChild1.elementType == JSStubElementTypes.VAR_STATEMENT
               && myChild2.elementType == JSTokenTypes.IDENTIFIER
               && myChild2.text == "of"
               && (node.psi as Angular2BlockParameter).let { it.isPrimaryExpression && it.block?.getName() == BLOCK_FOR }) {
        setSingleSpace(true)
      }
      else if (myChild1.elementType == JSTokenTypes.IDENTIFIER
               && myChild1.text == "of"
               && (node.psi as Angular2BlockParameter).let { it.isPrimaryExpression && it.block?.getName() == BLOCK_FOR }) {
        setSingleSpace(myChild2.elementType != TokenType.ERROR_ELEMENT)
      }
      else if (myChild1.elementType == JSTokenTypes.LPAR
               || myChild2.elementType == JSTokenTypes.LPAR
               || myChild2.elementType == JSTokenTypes.RPAR) {
        setSingleSpace(false)
      }
    }

    private fun visitDeferredTimeLiteralExpression() {
      if (myChild1.elementType == JSTokenTypes.NUMERIC_LITERAL && myChild2.elementType == JSTokenTypes.IDENTIFIER) {
        setSingleSpace(false)
      }
    }

  }

}