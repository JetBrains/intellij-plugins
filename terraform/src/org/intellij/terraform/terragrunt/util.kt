// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt

import com.intellij.psi.PsiFile
import org.intellij.terraform.terragrunt.model.StackRootBlocks
import org.intellij.terraform.terragrunt.model.TerragruntRootBlocks
import org.jetbrains.annotations.TestOnly

@TestOnly
internal val TerragruntBlockKeywords: List<String> = TerragruntRootBlocks.map { it.literal }

@TestOnly
internal val StackBlockKeywords: List<String> = StackRootBlocks.map { it.literal }

internal fun isTerragruntPsiFile(file: PsiFile?): Boolean = file?.fileType == TerragruntFileType

internal fun isTerragruntStack(file: PsiFile?): Boolean = isTerragruntPsiFile(file) && file?.virtualFile?.name == TERRAGRUNT_STACK_EXTENSION