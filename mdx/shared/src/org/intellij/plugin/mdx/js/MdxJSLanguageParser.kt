package org.intellij.plugin.mdx.js

import com.intellij.lang.PsiBuilder
import com.intellij.lang.WhitespacesBinders
import com.intellij.lang.ecmascript6.parsing.ES6Parser
import com.intellij.lang.ecmascript6.parsing.ES6StatementParser
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.JavascriptParserDefinition
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.psi.tree.IFileElementType

val FILE: IFileElementType = JSFileElementType.create(MdxJSLanguage.INSTANCE)

internal class MdxJSParserDefinition : JavascriptParserDefinition() {
  override fun getFileNodeType(): IFileElementType = FILE

  override fun createJSParser(builder: PsiBuilder): JavaScriptParser {
    return MdxJSLanguageParser(builder)
  }
}


internal class MdxJSLanguageParser(
  builder: PsiBuilder,
) : ES6Parser(
  JavascriptLanguage,
  builder,
) {

  override val statementParser: ES6StatementParser<*> =
    object : ES6StatementParser<MdxJSLanguageParser>(this@MdxJSLanguageParser) {
      override fun parseStatement() {
        if (builder.tokenType == JSTokenTypes.XML_START_TAG_START) {
          val exprStatement = this.builder.mark()
          if (parser.expressionParser.parseExpressionOptional()) {
            exprStatement.done(JSElementTypes.EXPRESSION_STATEMENT)
            exprStatement.setCustomEdgeTokenBinders(INCLUDE_DOC_COMMENT_AT_LEFT_NO_EXTRA_LINEBREAK,
                                                    WhitespacesBinders.DEFAULT_RIGHT_BINDER)
          }
          else {
            exprStatement.drop()
          }
        }
        else {
          super.parseStatement()
        }
      }
    }
}
