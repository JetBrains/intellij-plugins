// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
