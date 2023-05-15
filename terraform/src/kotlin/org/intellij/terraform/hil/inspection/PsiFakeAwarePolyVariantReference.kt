// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.inspection

import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveResult

interface PsiFakeAwarePolyVariantReference : PsiPolyVariantReference {
  fun multiResolve(incompleteCode: Boolean, includeFake: Boolean): Array<out ResolveResult>
}