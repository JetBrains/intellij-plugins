// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.options

import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.application.options.codeStyle.arrangement.ArrangementSettingsPanel
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CodeStyleSettings
import org.angular2.lang.html.Angular17HtmlFileType
import org.angular2.lang.html.Angular2HtmlLanguage

class Angular2CodeStyleMainPanel(currentSettings: CodeStyleSettings?, settings: CodeStyleSettings)
  : TabbedLanguageCodeStylePanel(Angular2HtmlLanguage, currentSettings, settings) {

  private val previewFileType get() = Angular17HtmlFileType

  override fun initTabs(settings: CodeStyleSettings) {
    addSpacesTab(settings)
    addWrappingAndBracesTab(settings)
    addTab(ArrangementSettingsPanel(settings, Angular2HtmlLanguage))
  }

  override fun addSpacesTab(settings: CodeStyleSettings?) {
    addTab(object : MySpacesPanel(settings) {
      override fun doReformat(project: Project, psiFile: PsiFile): PsiFile {
        CodeStyleManager.getInstance(project).reformatText(psiFile, 0, psiFile.textLength)
        return psiFile
      }

      override fun getFileType(): FileType = previewFileType
    })
  }

  override fun addWrappingAndBracesTab(settings: CodeStyleSettings?) {
    addTab(object : MyWrappingAndBracesPanel(settings) {
      override fun doReformat(project: Project, psiFile: PsiFile): PsiFile {
        CodeStyleManager.getInstance(project).reformatText(psiFile, 0, psiFile.textLength)
        return psiFile
      }

      override fun getFileType(): FileType = previewFileType
    })
  }

}
