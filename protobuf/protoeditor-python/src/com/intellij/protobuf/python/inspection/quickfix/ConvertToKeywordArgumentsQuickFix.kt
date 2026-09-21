package com.intellij.protobuf.python.inspection.quickfix

import com.intellij.codeInsight.intention.FileModifier
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.protobuf.python.PbPythonBundle
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.psi.LanguageLevel
import com.jetbrains.python.psi.PyArgumentList
import com.jetbrains.python.psi.PyElementGenerator
import com.jetbrains.python.psi.PyKeywordArgument
import com.jetbrains.python.psi.PyStarArgument

/**
 * Converts all positional arguments of a call to keyword arguments.
 * A partial conversion would leave a positional argument after a keyword argument, which is a syntax error.
 *
 * @param names The keyword for each positional argument, in the order of the arguments
 */
internal class ConvertToKeywordArgumentsQuickFix(@FileModifier.SafeFieldForPreview private val names: List<String>) : LocalQuickFix {
  override fun getFamilyName(): String = PbPythonBundle.message("quickfix.convert.to.keyword.arguments")

  override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
    val argumentList = descriptor.psiElement.parentOfType<PyArgumentList>() ?: return
    val positionalArguments = argumentList.arguments.filter { it !is PyKeywordArgument && it !is PyStarArgument }
    if (positionalArguments.size != names.size) return

    val generator = PyElementGenerator.getInstance(project)
    val languageLevel = LanguageLevel.forElement(argumentList)
    for ((argument, name) in positionalArguments.zip(names)) {
      argument.replace(generator.createKeywordArgument(languageLevel, name, argument.text))
    }
  }
}
