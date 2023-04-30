package com.intellij.dts.highlighting

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor

enum class DtsTextAttributes() {
    COMMENT("DTS_COMMENT", "Comment", DefaultLanguageHighlighterColors.LINE_COMMENT),
    COMPILER_DIRECTIVE("DTS_COMPILER_DIRECTIVE" , "Compiler directive", DefaultLanguageHighlighterColors.METADATA),
    STRING("DTS_STRING", "String//String Text", DefaultLanguageHighlighterColors.STRING),
    STRING_ESCAPE("DTS_STRING_ESCAPE", "String//Escape Sequence", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE),
    NODE_NAME("DTS_NODE_NAME", "Node//Name//Name Text", DefaultLanguageHighlighterColors.KEYWORD),
    NODE_UNIT_ADDR("DTS_NODE_UNIT_ADDR", "Node//Name//Unit Address", null),
    PROPERTY_NAME("DTS_PROP_NAME", "Node//Property Name", DefaultLanguageHighlighterColors.INSTANCE_FIELD),
    BRACES("DTS_BRACES", "Braces and Operators//Braces", DefaultLanguageHighlighterColors.BRACES),
    BRACKETS("DTS_BRACKETS", "Braces and Operators//Brackets", DefaultLanguageHighlighterColors.BRACKETS),
    OPERATOR("DTS_OPERATOR", "Braces and Operators//Operators", DefaultLanguageHighlighterColors.OPERATION_SIGN),
    COMMA("DTS_COMMA", "Braces and Operators//Comma", DefaultLanguageHighlighterColors.COMMA),
    SEMICOLON("DTS_SEMICOLON", "Braces and Operators//Semicolon", DefaultLanguageHighlighterColors.SEMICOLON),
    LABEL("DTS_LABEL", "Label", DefaultLanguageHighlighterColors.LABEL),
    NUMBER("DTS_NUMBER", "Number", DefaultLanguageHighlighterColors.NUMBER),
    BAD_CHARACTER("DTS_BAD_CHARACTER", "Bad character", HighlighterColors.BAD_CHARACTER);

    lateinit var attribute: TextAttributesKey
        private set

    lateinit var descriptor: AttributesDescriptor
        private set

    constructor(externalName: String, displayName: String, fallbackKey: TextAttributesKey?) : this() {
        attribute = TextAttributesKey.createTextAttributesKey(externalName, fallbackKey)
        descriptor = AttributesDescriptor(displayName, attribute)
    }
}