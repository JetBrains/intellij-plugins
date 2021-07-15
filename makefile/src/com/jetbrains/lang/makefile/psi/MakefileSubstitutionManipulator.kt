package com.jetbrains.lang.makefile.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

/**
 * A `Makefile` _substitution_ may be a host for injected shell script, e. g.:
 *
 * ```
 * `uname -a`
 * ```
 *
 * Here, the backticks denote a _substitution_ (a sub-shell invocation), and
 * `uname -a` is an injected shell script fragment.
 */
class MakefileSubstitutionManipulator : AbstractElementManipulator<MakefileSubstitution>() {
  override fun handleContentChange(element: MakefileSubstitution, range: TextRange, newContent: String): MakefileSubstitution =
    element.updateText(newContent) as MakefileSubstitution
}
