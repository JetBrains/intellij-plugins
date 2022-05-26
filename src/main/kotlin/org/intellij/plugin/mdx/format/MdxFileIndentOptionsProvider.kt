package org.intellij.plugin.mdx.format

import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.IndentOptions
import com.intellij.psi.codeStyle.FileIndentOptionsProvider
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider
import org.intellij.plugin.mdx.lang.psi.MdxFile

class MdxFileIndentOptionsProvider : FileIndentOptionsProvider() {
    override fun getIndentOptions(settings: CodeStyleSettings, file: PsiFile): IndentOptions? {
        if (file is MdxFile) {
            if (file.viewProvider is TemplateLanguageFileViewProvider) {
                val language = (file.viewProvider as TemplateLanguageFileViewProvider).templateDataLanguage
                return settings.getCommonSettings(language).indentOptions
            }
        }
        return null
    }
}