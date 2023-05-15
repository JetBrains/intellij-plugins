// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import org.intellij.terraform.hil.HILFileType

open class ILElementGenerator(val project: Project) {
  fun createILVariable(text: String): ILVariable {
    val file = createDummyFile(text)
    val firstChild = file.firstChild
    if (firstChild is ILExpressionHolder) {
      return firstChild.expression as ILVariable
    }
    return firstChild as ILVariable
  }

  fun createVarReference(name: String): ILSelectExpression {
    val file = createDummyFile("var.$name")
    val firstChild = file.firstChild
    if (firstChild is ILExpressionHolder) {
      return firstChild.expression as ILSelectExpression
    }
    return firstChild as ILSelectExpression
  }

  open fun createDummyFile(content: String): PsiFile {
    var code = content
    if (!code.startsWith("\${") && !code.endsWith("}")) {
      code = "\${$code}"
    }
    val psiFileFactory = PsiFileFactory.getInstance(project)
    return psiFileFactory.createFileFromText("dummy." + HILFileType.defaultExtension, HILFileType, code)
  }
}
