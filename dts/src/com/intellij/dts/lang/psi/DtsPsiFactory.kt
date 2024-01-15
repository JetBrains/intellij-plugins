package com.intellij.dts.lang.psi

import com.intellij.dts.lang.DtsFile
import com.intellij.dts.lang.DtsFileType
import com.intellij.dts.lang.DtsPropertyType
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

  private fun propertyValueTemplate(type: DtsPropertyType): String {
    return when (type) {
      DtsPropertyType.String, DtsPropertyType.StringList -> " = \"\""
      DtsPropertyType.Int, DtsPropertyType.Ints -> " = <>"
      DtsPropertyType.PHandle, DtsPropertyType.PHandles, DtsPropertyType.PHandleList -> " = <&>"
      DtsPropertyType.Boolean -> ""
      DtsPropertyType.Bytes -> " = []"
      DtsPropertyType.Path, DtsPropertyType.Compound -> " = "
    }
  }

  fun createProperty(project: Project, name: String, type: DtsPropertyType): DtsEntry {
    return createFile(
      project,
      "$name${propertyValueTemplate(type)};"
    ).dtsEntries.single()
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