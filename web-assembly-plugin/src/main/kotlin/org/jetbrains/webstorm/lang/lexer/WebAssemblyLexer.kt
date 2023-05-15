package org.jetbrains.webstorm.lang.lexer

import com.intellij.lexer.FlexAdapter
import java.io.Reader

class WebAssemblyLexer : FlexAdapter(_WebAssemblyLexer(null as Reader?))