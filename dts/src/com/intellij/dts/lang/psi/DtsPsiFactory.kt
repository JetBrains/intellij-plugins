package com.intellij.dts.lang.psi

import com.intellij.dts.completion.insert.dtsInsertIntoString
import com.intellij.dts.completion.insert.writePropertyValue
import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.DtsFileType
import com.intellij.dts.lang.symbols.DtsPropertySymbol
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.util.LocalTimeCounter

object DtsPsiFactory {
  // copied from com.intellij.protobuf.lang.psi.util.PbPsiFactory
  private fun createFile(project: Project, content: String): DtsFile {
    return PsiFileFactory.getInstance(project).createFileFromText(
      "file.dtsi",
      DtsFileType,
      content,
      LocalTimeCounter.currentTime(),
      false,
      true
    ) as DtsFile
  }

  fun createProperty(project: Project, symbol: DtsPropertySymbol): DtsEntry {
    val property = dtsInsertIntoString {
      write(symbol.name)
      writePropertyValue(symbol)
    }

    return createFile(project, property).dtsEntries.single()
  }

  fun createNodeContent(project: Project): DtsNodeContent {
    val node = createFile(
      project,
      "/ { a; };"
    ).dtsStatements.single() as DtsNode

    val content = node.dtsContent!!
    content.children.forEach(PsiElement::delete)

    return content
  }
}