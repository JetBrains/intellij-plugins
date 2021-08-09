// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.types

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeGuard

fun JSType.optionalIf(value: Boolean): JSType =
  if (value && this.source.isTypeScript)
    TypeScriptTypeGuard.wrapWithUndefined(this, this.source) ?: this
  else this