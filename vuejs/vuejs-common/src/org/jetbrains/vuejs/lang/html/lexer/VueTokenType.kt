// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.lexer

import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueTokenType(@NonNls debugName: String) : IElementType(debugName, VueLanguage)