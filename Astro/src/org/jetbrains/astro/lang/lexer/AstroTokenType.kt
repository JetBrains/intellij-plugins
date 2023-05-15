// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.lexer

import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls
import org.jetbrains.astro.lang.AstroLanguage

class AstroTokenType(@NonNls debugName: String) : IElementType(debugName, AstroLanguage.INSTANCE)
