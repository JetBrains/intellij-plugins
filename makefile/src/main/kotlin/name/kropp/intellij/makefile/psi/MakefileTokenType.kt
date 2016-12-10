package name.kropp.intellij.makefile.psi

import com.intellij.psi.tree.IElementType
import name.kropp.intellij.makefile.MakefileLanguage

open class MakefileTokenType(debugName: String) : IElementType(debugName, MakefileLanguage)

open class MakefileElementType(debugName: String) : IElementType(debugName, MakefileLanguage)