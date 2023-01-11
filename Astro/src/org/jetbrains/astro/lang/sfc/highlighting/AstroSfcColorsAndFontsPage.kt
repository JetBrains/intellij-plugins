// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.sfc.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.psi.codeStyle.DisplayPriority
import com.intellij.psi.codeStyle.DisplayPrioritySortable
import org.jetbrains.astro.AstroBundle
import org.jetbrains.astro.AstroIcons
import org.jetbrains.astro.lang.sfc.AstroSfcFileType
import javax.swing.Icon

class AstroSfcColorsAndFontsPage : ColorSettingsPage, DisplayPrioritySortable {
  override fun getDisplayName(): String {
    return AstroBundle.message("astro.colors.title")
  }

  override fun getIcon(): Icon {
    return AstroIcons.Astro
  }

  override fun getAttributeDescriptors(): Array<AttributesDescriptor> {
    return ATTRS
  }

  override fun getColorDescriptors(): Array<ColorDescriptor> {
    return ColorDescriptor.EMPTY_ARRAY
  }

  override fun getHighlighter(): SyntaxHighlighter {
    return SyntaxHighlighterFactory.getSyntaxHighlighter(AstroSfcFileType.INSTANCE, null, null)!!
  }

  override fun getDemoText(): String {
    return """
      ---
      export interface Props {
        title: string
      }
      const {title} = Astro.props
      ---
      This is a demo of {title}.
      """.trimIndent()
  }

  override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? {
    return null
  }

  override fun getPriority(): DisplayPriority {
    return DisplayPriority.LANGUAGE_SETTINGS
  }

  companion object {
    private val ATTRS: Array<AttributesDescriptor> = arrayOf(
      AttributesDescriptor(AstroBundle.message("astro.colors.frontmatter"),
                           AstroSfcHighlighterColors.ASTRO_FRONTMATTER),
      AttributesDescriptor(AstroBundle.message("astro.colors.frontmatter.separator"),
                           AstroSfcHighlighterColors.ASTRO_FRONTMATTER_SEPARATOR),
    )
  }
}