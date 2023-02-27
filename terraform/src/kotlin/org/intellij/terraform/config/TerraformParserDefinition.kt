/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.intellij.terraform.config

import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import org.intellij.terraform.hcl.HCLCapability
import org.intellij.terraform.hcl.HCLLexer
import org.intellij.terraform.hcl.HCLParserDefinition
import org.intellij.terraform.hcl.psi.impl.HCLFileImpl
import org.jetbrains.annotations.NotNull
import java.util.*

open class TerraformParserDefinition : HCLParserDefinition() {
  companion object {
    val FILE: IFileElementType = IFileElementType(TerraformLanguage)

    @JvmStatic
    fun createLexer(): HCLLexer {
      return HCLLexer(EnumSet.of(HCLCapability.INTERPOLATION_LANGUAGE))
    }
  }

  // TODO: Add special parser with psi elements in terms of Terraform (resource, provider, etc)

  override fun createLexer(project: Project): Lexer = createLexer()

  override fun getFileNodeType(): IFileElementType {
    return FILE
  }

  override fun createFile(fileViewProvider: @NotNull FileViewProvider): @NotNull PsiFile {
    return HCLFileImpl(fileViewProvider, TerraformLanguage)
  }
}
