package org.intellij.plugin.mdx.js

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.WhitespacesBinders
import com.intellij.lang.ecmascript6.parsing.ES6ExpressionParser
import com.intellij.lang.ecmascript6.parsing.ES6FunctionParser
import com.intellij.lang.ecmascript6.parsing.ES6Parser
import com.intellij.lang.ecmascript6.parsing.ES6StatementParser
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.dialects.ECMA6ParserDefinition
import com.intellij.lang.javascript.parsing.JSPsiTypeParser
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.lang.javascript.parsing.JavaScriptParserBase
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IFileElementType

class MdxJSParserDefinition : ECMA6ParserDefinition() {
    companion object {
        private val FILE: IFileElementType = JSFileElementType.create(MdxJSLanguage.INSTANCE)
    }

    override fun getFileNodeType(): IFileElementType {
        return FILE
    }

    override fun createParser(project: Project?): PsiParser {
        return PsiParser { root, builder ->
            MdxJSLanguageParser(builder).parseJS(root)
            return@PsiParser builder.treeBuilt
        }
    }
}


class MdxJSLanguageParser(builder: PsiBuilder) : ES6Parser<ES6ExpressionParser<*>,
        ES6StatementParser<*>,
        ES6FunctionParser<*>,
        JSPsiTypeParser<JavaScriptParser<*, *, *, *>>>(com.intellij.lang.javascript.JavaScriptSupportLoader.JSX_HARMONY, builder) {
    init {
        myStatementParser = object : ES6StatementParser<MdxJSLanguageParser>(this) {
            override fun parseSourceElement() {
                if (builder.tokenType == JSTokenTypes.XML_START_TAG_START) {
                    val exprStatement = this.builder.mark()
                    if (myJavaScriptParser.expressionParser.parseExpressionOptional()) {
                        exprStatement.done(JSElementTypes.EXPRESSION_STATEMENT)
                        exprStatement.setCustomEdgeTokenBinders(JavaScriptParserBase.INCLUDE_DOC_COMMENT_AT_LEFT_NO_EXTRA_LINEBREAK, WhitespacesBinders.DEFAULT_RIGHT_BINDER)
                    } else {
                        exprStatement.drop()
                    }
                } else {
                    super.parseSourceElement()
                }
            }

        }
    }

}