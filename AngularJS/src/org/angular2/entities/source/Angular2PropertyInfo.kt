// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

data class Angular2PropertyInfo constructor(
  val name: String,
  val required: Boolean,
  val declaringElement: PsiElement?,
  val declarationRange: TextRange? = if (declaringElement != null) TextRange(1, 1 + name.length) else null,
)