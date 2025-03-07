// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.refactoring

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLStringLiteral
import org.intellij.terraform.isTerraformCompatiblePsiFile

internal class TfVetoRenameCondition : Condition<PsiElement> {
  override fun value(element: PsiElement?): Boolean {
    if (element !is HCLElement) return false

    val file = element.containingFile ?: return true
    if (!isTerraformCompatiblePsiFile(file)) {
      return true
    }

    val targetElement = (element as? HCLIdentifier) ?: getCaretElement(element, file) ?: return true
    return !isRenameAllowed(targetElement)
  }

  private fun getCaretElement(element: PsiElement, file: PsiFile): HCLElement? {
    val project = element.project
    val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return null
    val caretElement = file.findElementAt(editor.caretModel.offset) ?: return null

    val identifier = caretElement.parent
    return if (identifier is HCLIdentifier || identifier is HCLStringLiteral) {
      identifier
    } else null
  }

  private fun isRenameAllowed(element: HCLElement): Boolean {
    val parent = element.parent
    return when (parent) {
      is HCLBlock -> isBlockRenamingAllowed(parent) && parent.nameIdentifier === element
      is HCLProperty -> isLocalProperty(parent) && parent.nameIdentifier === element
      else -> false
    }
  }

  private fun isBlockRenamingAllowed(block: HCLBlock): Boolean = listOf(
    TfPsiPatterns.ResourceRootBlock,
    TfPsiPatterns.DataSourceRootBlock,
    TfPsiPatterns.VariableRootBlock,
    TfPsiPatterns.OutputRootBlock,
    TfPsiPatterns.ModuleRootBlock,
    TfPsiPatterns.ProviderRootBlock
  ).any { it.accepts(block) }

  private fun isLocalProperty(property: HCLProperty): Boolean {
    val block = property.parentOfType<HCLBlock>()
    return TfPsiPatterns.LocalsRootBlock.accepts(block)
  }
}