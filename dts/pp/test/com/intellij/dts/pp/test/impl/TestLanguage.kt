package com.intellij.dts.pp.test.impl

import com.intellij.lang.Language
import com.intellij.psi.tree.IElementType

object TestLanguage : Language("test")

class TestTokenType(debugName: String) : IElementType(debugName, TestLanguage)

class TestElementType(debugName: String) : IElementType(debugName, TestLanguage)
