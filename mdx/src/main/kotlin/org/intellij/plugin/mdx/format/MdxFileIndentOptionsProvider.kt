package org.intellij.plugin.mdx.format

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.IndentOptions
import com.intellij.psi.codeStyle.FileIndentOptionsProvider
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider
import org.intellij.plugin.mdx.lang.MdxFileType

class MdxFileIndentOptionsProvider : FileIndentOptionsProvider() {
  override fun getIndentOptions(project: Project, settings: CodeStyleSettings, file: VirtualFile): IndentOptions? {
    if (file.fileType is MdxFileType) {
      val viewProvider = PsiManagerEx.getInstanceEx(project).findViewProvider(file)
      if (viewProvider is TemplateLanguageFileViewProvider) {
        val language = viewProvider.templateDataLanguage
        return settings.getCommonSettings(language).indentOptions
      }
    }
    return null
  }
}