package com.jetbrains.lang.makefile.psi

import com.intellij.psi.tree.IElementType
import com.jetbrains.lang.makefile.MakefileLanguage

open class MakefileTokenType(debugName: String) : IElementType(debugName, MakefileLanguage)

open class MakefileElementType(debugName: String) : IElementType(debugName, MakefileLanguage)