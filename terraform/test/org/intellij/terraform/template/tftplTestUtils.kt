package org.intellij.terraform.template

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import com.intellij.testFramework.IndexingTestUtil

internal fun withDataLanguageForFile(virtualFile: VirtualFile, language: Language, project: Project, test: () -> Unit) {
  try {
    TemplateDataLanguageMappings.getInstance(project).setMapping(virtualFile, language)
    IndexingTestUtil.waitUntilIndexesAreReady(project)
    test()
  }
  finally {
    TemplateDataLanguageMappings.getInstance(project).setMapping(virtualFile, null)
    IndexingTestUtil.waitUntilIndexesAreReady(project)
  }
}
