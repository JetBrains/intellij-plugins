// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma

import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import java.io.File

private const val PRISMA_TEST_DATA_PATH = "/prisma/testData"

internal fun getPrismaTestDataPath(): String = getContribPath() + PRISMA_TEST_DATA_PATH

internal fun getPrismaRelativeTestDataPath(): String = "/contrib$PRISMA_TEST_DATA_PATH"

private fun getContribPath(): String {
  val homePath = IdeaTestExecutionPolicy.getHomePathWithPolicy()
  return if (File(homePath, "contrib/.gitignore").isFile) {
    homePath + File.separatorChar + "contrib"
  }
  else homePath
}

internal fun reformatDocumentation(project: Project, text: String): String {
  return WriteCommandAction.runWriteCommandAction(project, Computable {
    PsiFileFactory.getInstance(project).createFileFromText("doc.html", HTMLLanguage.INSTANCE, text)
      .let { CodeStyleManager.getInstance(project).reformat(it) }
      .text
  })
}