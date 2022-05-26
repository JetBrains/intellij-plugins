package org.intellij.plugin.mdx.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import org.intellij.plugin.mdx.ide.MDXIcons
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

class MdxFileType private constructor() : LanguageFileType(MdxLanguage) {
    override fun getName(): String {
        return "MDX"
    }

    override fun getDescription(): @Nls(capitalization = Nls.Capitalization.Sentence) String {
        return "MDX"
    }


    override fun getDefaultExtension(): String {
        return DEFAULT_EXTENSION
    }

    override fun getIcon(): Icon? {
        return MDXIcons.FILE
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