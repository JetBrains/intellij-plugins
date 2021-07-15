package com.jetbrains.lang.makefile.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

/**
 * A `Makefile` _recipe_ may be a host for injected shell script (each _recipe_
 * line, after a leading tab and optional `@` or `-` characters, is an injected
 * shell script fragment).
 */
class MakefileRecipeManipulator : AbstractElementManipulator<MakefileRecipe>() {
  override fun handleContentChange(element: MakefileRecipe, range: TextRange, newContent: String): MakefileRecipe =
    element.updateText(newContent) as MakefileRecipe
}
