// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElementVisitor
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.opentofu.OpenTofuConstants
import org.intellij.terraform.opentofu.OpenTofuFileType
import org.intellij.terraform.opentofu.patterns.OpenTofuPatterns

internal class OpenTofuUnknownKeyProviderInspection : LocalInspectionTool() {

  override fun isAvailableForFile(file: PsiFile): Boolean {
    return file.fileType == OpenTofuFileType
  }

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
    return EncryptionMethodKeysVisitor(holder)
  }

  inner class EncryptionMethodKeysVisitor(val holder: ProblemsHolder) : HCLElementVisitor() {
    override fun visitIdentifier(identifier: HCLIdentifier) {
      if (!OpenTofuPatterns.EncryptionMethodKeysPropertyValue.accepts(identifier)) return
      if (identifier.references
          .mapNotNull { reference -> reference.resolve() }
          .none { it is HCLBlock && it.getNameElementUnquoted(0) == OpenTofuConstants.TOFU_KEY_PROVIDER }) {
        holder.registerProblem(identifier, HCLBundle.message("opentofu.unknown.key.provider.inspection.message", identifier.name))
      }
    }
  }
}