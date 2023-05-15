package com.intellij.dts.settings

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.dts.highlighting.DtsSyntaxHighlighter
import com.intellij.dts.highlighting.DtsTextAttributes
import com.intellij.dts.lang.DtsLanguage
import javax.swing.Icon

class DtsColorSettingsPage : ColorSettingsPage {
    override fun getDisplayName(): String = DtsLanguage.displayName

    override fun getIcon(): Icon? = null

    override fun getHighlighter(): SyntaxHighlighter = DtsSyntaxHighlighter()

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDemoText(): String = Codesamples.coloring

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> {
        return DtsTextAttributes.values().map { it.descriptor }.toTypedArray()
    }

    override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey> {
        return mutableMapOf(
                "ESCAPED" to DtsTextAttributes.STRING_ESCAPE.attribute,
                "LABEL" to DtsTextAttributes.LABEL.attribute,
                "PROPERTY" to DtsTextAttributes.PROPERTY_NAME.attribute,
                "NODE_NAME" to DtsTextAttributes.NODE_NAME.attribute,
                "NODE_ADDR" to DtsTextAttributes.NODE_UNIT_ADDR.attribute
        )
    }
}