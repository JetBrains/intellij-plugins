package org.intellij.plugin.mdx.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import org.intellij.plugin.mdx.MdxBundle
import org.jetbrains.annotations.Nls
import javax.swing.Icon

internal object MdxFileType : LanguageFileType(MdxLanguage) {
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

  const val DEFAULT_EXTENSION = "mdx"
}
