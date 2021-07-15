package com.jetbrains.lang.makefile.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

/**
 * A `Makefile` _function_ may be a host for injected shell script, e. g.:
 *
 * ```
 * $(shell uname -a)
 * ```
 *
 * Here, `$(shell)` is a _function_, and `uname -a` is an injected shell
 * script fragment.
 */
class MakefileFunctionManipulator : AbstractElementManipulator<MakefileFunction>() {
  override fun handleContentChange(element: MakefileFunction, range: TextRange, newContent: String): MakefileFunction =
    element.updateText(newContent) as MakefileFunction
}
