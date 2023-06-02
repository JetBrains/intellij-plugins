package com.intellij.dts.highlighting

import com.intellij.dts.DtsBundle
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.annotations.PropertyKey

enum class DtsTextAttributes() {
    STRING(
        "DTS_STRING",
        DefaultLanguageHighlighterColors.STRING,
        "settings.colors.group.string", "settings.colors.string.text"
    ),
    STRING_ESCAPE(
        "DTS_STRING_ESCAPE",
        DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE,
        "settings.colors.group.string", "settings.colors.string.escape"
    ),
    NODE_NAME(
        "DTS_NODE_NAME",
        DefaultLanguageHighlighterColors.KEYWORD,
        "settings.colors.group.node", "settings.colors.group.node.node_name", "settings.colors.node.node_name.name_text"
    ),
    NODE_UNIT_ADDR(
        "DTS_NODE_UNIT_ADDR",
        null,
        "settings.colors.group.node", "settings.colors.group.node.node_name", "settings.colors.node.node_name.unit_address"
    ),
    PROPERTY_NAME(
        "DTS_PROP_NAME",
        DefaultLanguageHighlighterColors.INSTANCE_FIELD,
        "settings.colors.group.node", "settings.colors.node.property_name"
    ),
    BRACES(
        "DTS_BRACES",
        DefaultLanguageHighlighterColors.BRACES,
        "settings.colors.group.bao", "settings.colors.bao.braces"
    ),
    BRACKETS(
        "DTS_BRACKETS",
        DefaultLanguageHighlighterColors.BRACKETS,
        "settings.colors.group.bao", "settings.colors.bao.brackets"
    ),
    OPERATOR(
        "DTS_OPERATOR",
        DefaultLanguageHighlighterColors.OPERATION_SIGN,
        "settings.colors.group.bao", "settings.colors.bao.operators"
    ),
    COMMA(
        "DTS_COMMA",
        DefaultLanguageHighlighterColors.COMMA,
        "settings.colors.group.bao", "settings.colors.bao.comma"
    ),
    SEMICOLON(
        "DTS_SEMICOLON",
        DefaultLanguageHighlighterColors.SEMICOLON,
        "settings.colors.group.bao", "settings.colors.bao.semicolon"
    ),
    COMPILER_DIRECTIVE(
        "DTS_COMPILER_DIRECTIVE",
        DefaultLanguageHighlighterColors.METADATA,
        "settings.colors.compiler_directive"
    ),
    COMMENT(
        "DTS_COMMENT",
        DefaultLanguageHighlighterColors.LINE_COMMENT,
        "settings.colors.comment"
    ),
    LABEL(
        "DTS_LABEL",
        DefaultLanguageHighlighterColors.LABEL,
        "settings.colors.label"
    ),
    NUMBER(
        "DTS_NUMBER",
        DefaultLanguageHighlighterColors.NUMBER,
        "settings.colors.number"
    ),
    BAD_CHARACTER(
        "DTS_BAD_CHARACTER",
        HighlighterColors.BAD_CHARACTER,
        "settings.colors.bad_char"
    );

    lateinit var attribute: TextAttributesKey
        private set

    lateinit var descriptor: AttributesDescriptor
        private set

    constructor(
        externalName: String,
        fallbackKey: TextAttributesKey?,
        vararg bundleKey: @PropertyKey(resourceBundle = DtsBundle.BUNDLE) String,
    ) : this() {
        attribute = TextAttributesKey.createTextAttributesKey(externalName, fallbackKey)
        descriptor = AttributesDescriptor(StringUtil.join(bundleKey.map { DtsBundle.message(it) }, "//"), attribute)
    }
}