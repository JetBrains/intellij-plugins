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
package org.intellij.terraform.config.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.DebugUtil
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLElementGenerator
import org.intellij.terraform.hcl.psi.HCLLiteral
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.config.model.Type
import org.intellij.terraform.hil.psi.ILExpression
import org.intellij.terraform.hil.psi.ILLiteralExpression

class TerraformElementGenerator(val project: Project) : HCLElementGenerator(project) {
  override fun createDummyFile(content: String): PsiFile {
    val psiFileFactory = PsiFileFactory.getInstance(project)
    val psiFile = psiFileFactory.createFileFromText("dummy." + TerraformFileType.defaultExtension, TerraformFileType, content)
    if (PsiTreeUtil.hasErrorElements(psiFile)) {
      throw IllegalStateException("PsiFile contains PsiErrorElement: " + DebugUtil.psiToString(psiFile, false, true))
    }
    return psiFile
  }

  fun createVariable(name: String, type: Type?, initializer: ILExpression): HCLBlock {
    // TODO: Improve
    val value = when (initializer) {
      is ILLiteralExpression -> initializer.text
      else -> "\"\${${initializer.text}}\""
    }
    return createVariable(name, type, value)
  }

  fun createVariable(name: String, type: Type?, value: String): HCLBlock {
    val content = buildString {
      append("variable \"").append(name).append("\" {")
      val typeName = when(type) {
        null -> null
        else -> type.presentableText.toLowerCase()
      }
      if (typeName != null) {
        append("\n  type=").append(typeName)
      }
      append("\n  default=").append(value).append("\n}")
    }
    val file = createDummyFile(content)
    return file.firstChild as HCLBlock
  }

  fun createVariable(name: String, type: Type?, initializer: HCLElement): PsiElement {
    // TODO: Improve
    val value = when (initializer) {
      is HCLLiteral -> initializer.text
      else -> initializer.text
    }
    return createVariable(name, type, value)
  }
}
