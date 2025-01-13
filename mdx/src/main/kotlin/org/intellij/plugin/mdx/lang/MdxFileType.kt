package org.intellij.plugin.mdx.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import org.intellij.plugin.mdx.MdxBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

internal class MdxFileType private constructor() : LanguageFileType(MdxLanguage) {
  override fun getName(): String {
    return "MDX"
  }

  override fun getDescription(): @Nls(capitalization = Nls.Capitalization.Sentence) String {
    return MdxBundle.message("mdx.file.type.description")
  }


  override fun getDefaultExtension(): String {
    return DEFAULT_EXTENSION
  }

  override fun getIcon(): Icon {
    return icons.MdxIcons.Mdx
  }

  override fun isReadOnly(): Boolean {
    return false
  }

  companion object {
    val INSTANCE: LanguageFileType = MdxFileType()

    @NonNls
    val DEFAULT_EXTENSION = "mdx"
  }
}