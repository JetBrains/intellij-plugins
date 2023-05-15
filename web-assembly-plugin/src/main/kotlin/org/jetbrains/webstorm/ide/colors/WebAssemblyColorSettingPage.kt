package org.jetbrains.webstorm.ide.colors

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import org.jetbrains.webstorm.ide.WebAssemblySyntaxHighlighter
import org.jetbrains.webstorm.ide.icons.WebAssemblyIcons
import javax.swing.Icon

class WebAssemblyColorSettingPage : ColorSettingsPage {
    private val attributesDescriptors = WebAssemblyColor.values().map { it.attributesDescriptor }.toTypedArray()
    private val tagToDescriptorMap = WebAssemblyColor.values().associateBy({ it.name }, { it.textAttributesKey })

    override fun getIcon(): Icon? = WebAssemblyIcons.WEB_ASSEMBLY_FILE
    override fun getHighlighter(): SyntaxHighlighter = WebAssemblySyntaxHighlighter()

    override fun getDemoText(): String =
            """
(module
  ;; one line comment
  (func ${'$'}getAnswer (result i32)
    i32.const 42)
  (func (export "getAnswerPlus1") (result i32)
    call ${'$'}getAnswer
    (; block
       comment ;)
    i32.const 1
    i32.add))
            """.trimIndent()
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = tagToDescriptorMap
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = attributesDescriptors
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getDisplayName(): String = "WebAssembly"
}