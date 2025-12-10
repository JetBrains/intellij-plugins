// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.actionscript.parsing

import com.intellij.lang.actionscript.ActionScriptElementTypes
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptParserBundle.message
import com.intellij.lang.javascript.parsing.FunctionParser
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeNameValuePairImpl
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.tree.IElementType

/**
 * @author Konstantin.Ulitin
 */
class ActionScriptFunctionParser internal constructor(parser: ActionScriptParser) : FunctionParser<ActionScriptParser>(parser) {
    override fun parseFunctionName(functionKeywordWasOmitted: Boolean, context: Context): Boolean {
        if (parseGetSetAndNameAfterFunctionKeyword(context)) return true
        return super.parseFunctionName(functionKeywordWasOmitted, context)
    }

    override fun parseFunctionIdentifier() {
        if (!JSKeywordSets.PROPERTY_NAMES.contains(builder.getTokenType())) {
            LOG.error(builder.getTokenText())
        }

        parser.statementParser.parsePossiblyQualifiedName()
    }

    override val functionDeclarationElementType: IElementType
        get() = ActionScriptElementTypes.ACTIONSCRIPT_FUNCTION

    override fun parseAttributesList(): Boolean {
        val modifierList = builder.mark()

        var seenNs = false
        var seenAnyAttributes = false

        try {
            var hasSomethingInAttrList = true
            var hadConditionalCompileBlock = false

            var doNotAllowAttributes = false
            while (hasSomethingInAttrList) {
                hasSomethingInAttrList = false

                while (builder.getTokenType() === JSTokenTypes.LBRACKET) {
                    if (doNotAllowAttributes) {
                        builder.error(message("javascript.parser.message.expected.declaration"))
                        break
                    }

                    val attribute = builder.mark()

                    builder.advanceLexer()

                    val tokenType = builder.getTokenType()
                    if (tokenType === JSTokenTypes.RBRACKET) {
                        builder.error(message("javascript.parser.message.expected.identifier"))
                    } else if (tokenType == null || !isIdentifierToken(tokenType)) {
                        attribute.drop()
                        return false
                    } else {
                        builder.advanceLexer()
                    }

                    while (builder.getTokenType() !== JSTokenTypes.RBRACKET) {
                        parseAttributeBody()

                        if (builder.eof()) {
                            attribute.done(JSElementTypes.ATTRIBUTE)
                            builder.error(message("javascript.parser.message.expected.rbracket"))
                            return true
                        }
                    }

                    builder.advanceLexer()
                    attribute.done(JSElementTypes.ATTRIBUTE)
                    hasSomethingInAttrList = true
                }

                if (builder.getTokenType() === JSTokenTypes.INCLUDE_KEYWORD) {
                    hasSomethingInAttrList = true
                    parser.statementParser.parseIncludeDirective()
                }

                if (builder.getTokenType() === JSTokenTypes.USE_KEYWORD && !doNotAllowAttributes) {
                    hasSomethingInAttrList = true
                    parser.statementParser.parseUseNamespaceDirective()
                }

                if (builder.getTokenType() === JSTokenTypes.IDENTIFIER && !seenNs) {
                    var identifier = builder.mark()
                    hasSomethingInAttrList = true
                    seenNs = true
                    val marker = builder.mark()
                    builder.advanceLexer()
                    marker.done(JSElementTypes.REFERENCE_EXPRESSION)

                    val tokenType = builder.getTokenType()

                    if (!hadConditionalCompileBlock) {
                        if (tokenType === JSTokenTypes.COLON_COLON &&
                            parser.expressionParser.proceedWithNamespaceReference(identifier, false)
                        ) {
                            (identifier.precede().also { identifier = it }).done(JSElementTypes.REFERENCE_EXPRESSION)
                            identifier.precede().done(JSElementTypes.CONDITIONAL_COMPILE_VARIABLE_REFERENCE)
                            hadConditionalCompileBlock = true
                            seenNs = false
                        } else if (tokenType === JSTokenTypes.DOT) {
                            while (builder.getTokenType() === JSTokenTypes.DOT) {
                                builder.advanceLexer()
                                val identifierToken = isIdentifierToken(builder.getTokenType())
                                if (identifierToken) {
                                    builder.advanceLexer()
                                }
                                identifier.done(JSElementTypes.REFERENCE_EXPRESSION)
                                identifier = identifier.precede()
                                if (!identifierToken) {
                                    builder.error(message("javascript.parser.message.expected.name"))
                                    break
                                }
                            }
                            identifier.drop()
                        } else {
                            identifier.drop()
                        }
                    } else {
                        identifier.drop()
                    }
                }

                var tokenType: IElementType?
                while (JSTokenTypes.MODIFIERS.contains(builder.getTokenType().also { tokenType = it })
                    || tokenType === JSTokenTypes.GET_KEYWORD || tokenType === JSTokenTypes.SET_KEYWORD
                ) {
                    doNotAllowAttributes = true
                    seenAnyAttributes = true
                    hasSomethingInAttrList = true
                    if (builder.getTokenType() === JSTokenTypes.NATIVE_KEYWORD ||
                        builder.getTokenType() === JSTokenTypes.DECLARE_KEYWORD
                    ) {
                        builder.putUserData(methodsEmptinessKey, MethodEmptiness.ALWAYS)
                    }
                    builder.advanceLexer()
                }

                if (builder.eof()) {
                    return true
                }
            }
        } finally {
            val currentTokenType = builder.getTokenType()

            if (seenNs && !seenAnyAttributes &&
                isNonAttrListOwner(currentTokenType)
            ) {
                modifierList.rollbackTo()
            } else {
                modifierList.done(attributeListElementType)
            }
        }
        return true
    }

    override val attributeListElementType: IElementType
        get() = ActionScriptElementTypes.ACTIONSCRIPT_ATTRIBUTE_LIST

    fun parseAttributeBody() {
        val haveLParen = checkMatches(builder, JSTokenTypes.LPAR, "javascript.parser.message.expected.lparen")

        while (haveLParen) {
            val hasName = JSAttributeNameValuePairImpl.IDENTIFIER_TOKENS_SET.contains(builder.getTokenType())

            if (builder.getTokenType() === JSTokenTypes.COMMA) {
                builder.error(message("javascript.parser.message.expected.identifier.or.value"))
                break
            }
            val tokenType = builder.getTokenType()
            if (tokenType === JSTokenTypes.RBRACKET) break
            if (tokenType === JSTokenTypes.RPAR) break

            val attributeNameValuePair = builder.mark()
            builder.advanceLexer()

            if (hasName && builder.getTokenType() !== JSTokenTypes.COMMA && builder.getTokenType() !== JSTokenTypes.RPAR) {
                checkMatches(builder, JSTokenTypes.EQ, "javascript.parser.message.expected.equal")

                val type = builder.getTokenType()
                if (type !== JSTokenTypes.COMMA && type !== JSTokenTypes.RBRACKET && type !== JSTokenTypes.RPAR) {
                    if (type === JSTokenTypes.IDENTIFIER) {
                        val ident = builder.mark()
                        builder.advanceLexer()
                        val nextTokenType = builder.getTokenType()
                        ident.rollbackTo()
                        if (!JSTokenTypes.STRING_LITERALS.contains(nextTokenType)) {
                            parser.expressionParser.parseSimpleExpression()
                        } else {
                            builder.advanceLexer()
                        }
                    } else {
                        builder.advanceLexer()
                    }
                } else {
                    builder.error(message("javascript.parser.message.expected.value"))
                }
            }

            attributeNameValuePair.done(JSElementTypes.ATTRIBUTE_NAME_VALUE_PAIR)
            if (builder.getTokenType() !== JSTokenTypes.COMMA) break
            builder.advanceLexer()

            if (builder.eof()) {
                builder.error(message("javascript.parser.message.expected.rparen"))
                return
            }
        }

        if (haveLParen) {
            checkMatches(builder, JSTokenTypes.RPAR, "javascript.parser.message.expected.rparen")
        } else {
            builder.advanceLexer()
        }
    }

    override fun parseFunctionExpressionAttributeList() {
        val mark = builder.mark()
        val type = builder.getTokenType()
        if (type === JSTokenTypes.GET_KEYWORD
            || type === JSTokenTypes.SET_KEYWORD
        ) {
            builder.advanceLexer()
        }
        mark.done(attributeListElementType)
    }

    override val functionExpressionElementType: IElementType
        get() = ActionScriptElementTypes.ACTIONSCRIPT_FUNCTION_EXPRESSION

    override val parameterType: IElementType
        get() = ActionScriptElementTypes.ACTIONSCRIPT_PARAMETER

    companion object {
        private val LOG = Logger.getInstance(ActionScriptFunctionParser::class.java)

        private fun isNonAttrListOwner(currentTokenType: IElementType?): Boolean {
            return currentTokenType !== JSTokenTypes.VAR_KEYWORD && currentTokenType !== JSTokenTypes.CONST_KEYWORD && currentTokenType !== JSTokenTypes.FUNCTION_KEYWORD && currentTokenType !== JSTokenTypes.CLASS_KEYWORD && currentTokenType !== JSTokenTypes.INTERFACE_KEYWORD && currentTokenType !== JSTokenTypes.NAMESPACE_KEYWORD
        }
    }
}
