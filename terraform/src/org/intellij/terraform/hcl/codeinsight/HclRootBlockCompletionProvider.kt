// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.codeinsight

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.PsiFilePattern
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.RootBlockSorted
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.createPropertyOrBlockType
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.patterns.HCLPatterns.Block
import org.intellij.terraform.hcl.patterns.HCLPatterns.File
import org.intellij.terraform.hcl.patterns.HCLPatterns.IdentifierOrStringLiteral
import org.intellij.terraform.hcl.patterns.HCLPatterns.IdentifierOrStringLiteralOrSimple
import org.intellij.terraform.hcl.patterns.HCLPatterns.WhiteSpace
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hcl.psi.afterSiblingSkipping2
import org.intellij.terraform.isTerraformCompatiblePsiFile
import org.intellij.terraform.terragrunt.isTerragruntPsiFile
import org.intellij.terraform.terragrunt.isTerragruntStack
import org.intellij.terraform.terragrunt.model.StackRootBlocks
import org.intellij.terraform.terragrunt.model.TerragruntRootBlocks
import kotlin.sequences.forEach

internal object HclRootBlockCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val position = parameters.position
    position.parent ?: return

    val psiFile = position.containingFile.originalFile
    val rootBlock = when {
      isTerraformCompatiblePsiFile(psiFile) -> RootBlockSorted
      isTerragruntPsiFile(psiFile) -> if (isTerragruntStack(psiFile)) StackRootBlocks else TerragruntRootBlocks
      else -> return
    }

    rootBlock.asSequence().filter { it.canBeUsedIn(psiFile.fileType) }.map { createPropertyOrBlockType(it) }.forEach {
      result.addElement(it)
    }
  }

  fun createRootBlockPattern(filePattern: PsiFilePattern.Capture<HCLFile>): PsiElementPattern.Capture<PsiElement> {
    return psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(filePattern)
      .withParent(File)
      .andNot(psiElement().afterSiblingSkipping2(WhiteSpace, IdentifierOrStringLiteralOrSimple))
  }

  fun createBlockHeaderPattern(filePattern: PsiFilePattern.Capture<HCLFile>): PsiElementPattern.Capture<PsiElement> {
    return psiElement().withElementType(HCLTokenTypes.IDENTIFYING_LITERALS)
      .inFile(filePattern)
      .withParent(psiElement()
                    .and(IdentifierOrStringLiteral)
                    .andNot(psiElement().afterSiblingSkipping2(WhiteSpace, IdentifierOrStringLiteralOrSimple)))
      .withSuperParent(2, Block)
      .withSuperParent(3, File)
  }
}