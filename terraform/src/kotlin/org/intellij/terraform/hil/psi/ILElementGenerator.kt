/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
