package com.intellij.protobuf.lang.psi.util

import com.intellij.openapi.project.Project
import com.intellij.protobuf.lang.PbFileType
import com.intellij.protobuf.lang.psi.PbFile
import com.intellij.protobuf.lang.psi.PbImportStatement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.util.LocalTimeCounter

internal object PbPsiFactory {
  fun createFileFromText(project: Project, content: String = "", name: String = "dummy.proto"): PbFile {
    return PsiFileFactory.getInstance(project).createFileFromText(
      name,
      PbFileType.INSTANCE,
      content,
      LocalTimeCounter.currentTime(),
      false,
      true
    ) as PbFile
  }

  fun createImportStatement(project: Project, importedFqn: String): PbImportStatement {
    return createFileFromText(
      project,
      """
        syntax = "proto3";
        import "$importedFqn";
      """.trimMargin()
    ).importStatements.single()
  }

  fun createNewLine(project: Project): PsiElement {
    return createFileFromText(project, "\n").firstChild
  }
}