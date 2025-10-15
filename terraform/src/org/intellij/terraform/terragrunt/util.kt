// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt

import com.intellij.psi.PsiFile

internal fun isTerragruntPsiFile(file: PsiFile?): Boolean = file?.fileType == TerragruntFileType

internal fun isTerragruntStack(file: PsiFile?): Boolean = isTerragruntPsiFile(file) && file?.virtualFile?.name == TERRAGRUNT_STACK_FILE