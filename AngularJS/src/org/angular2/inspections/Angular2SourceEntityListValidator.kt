// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.util.AstLoadingFilter
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.MultiMap
import com.intellij.util.containers.TreeTraversal
import org.angular2.Angular2DecoratorUtil.getProperty
import org.angular2.entities.Angular2Entity
import org.angular2.entities.source.Angular2SourceEntityListProcessor
import org.jetbrains.annotations.Nls

internal abstract class Angular2SourceEntityListValidator<T : Angular2Entity, E : Enum<*>>
protected constructor(private val decorator: ES6Decorator,
                      private val results: ValidationResults<in E>,
                      entityClass: Class<T>,
                      private val propertyName: String)
  : Angular2SourceEntityListProcessor<T>(entityClass) {
  private lateinit var myIterator: TreeTraversal.TracingIt<PsiElement>

  fun validate() {
    val property = getProperty(decorator, propertyName) ?: return
    AstLoadingFilter.forceAllowTreeLoading<RuntimeException>(property.containingFile) {
      val value = property.value
      if (value == null) {
        return@forceAllowTreeLoading
      }
      val visited = HashSet<PsiElement>()
      myIterator = TreeTraversal.LEAVES_DFS
        .traversal(listOf(value)) { element: PsiElement ->
          // Protect against cyclic references or visiting same thing several times
          if (visited.add(element)) resolve(element) else emptyList()
        }
        .typedIterator()
      while (myIterator.advance()) {
        ProgressManager.checkCanceled()
        myIterator.current().accept(resultsVisitor)
      }
    }
  }

  private fun locateProblemElement(): PsiElement {
    val file = decorator.containingFile.originalFile
    for (el in ContainerUtil.concat(listOf(myIterator.current()), myIterator.backtrace())) {
      if (file == el.containingFile.originalFile && decorator.textRange.contains(el.textRange)) {
        return el
      }
    }
    return decorator
  }

  protected fun registerProblem(problemType: E,
                                @Nls message: String,
                                vararg quickFixes: LocalQuickFix) {
    results.registerProblem(locateProblemElement(), problemType, message,
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING, *quickFixes)
  }

  protected fun registerProblem(problemType: E,
                                @Nls message: String,
                                severity: ProblemHighlightType,
                                vararg quickFixes: LocalQuickFix) {
    results.registerProblem(locateProblemElement(), problemType, message,
                            severity, *quickFixes)
  }

  interface ValidationProblem {
    val location: PsiElement

    val message: @Nls String

    val severity: ProblemHighlightType

    val fixes: Array<out LocalQuickFix>
  }

  open class ValidationResults<T : Enum<*>> {

    private val results = MultiMap<T, ValidationProblem>()

    open fun registerProblems(problemType: T, holder: ProblemsHolder) {
      for (problem in results.get(problemType)) {
        holder.registerProblem(problem.location, problem.message, problem.severity, *problem.fixes)
      }
    }

    internal fun registerProblem(element: PsiElement,
                                 type: T,
                                 @Nls message: String,
                                 severity: ProblemHighlightType,
                                 vararg quickFixes: LocalQuickFix) {
      results.putValue(type, object : ValidationProblem {
        override val message: String
          get() = message

        override val location: PsiElement
          get() = element

        override val severity: ProblemHighlightType
          get() = severity

        override val fixes: Array<out LocalQuickFix>
          get() = quickFixes
      })
    }

    companion object {

      fun <T : Enum<*>> empty(): ValidationResults<T> {
        @Suppress("UNCHECKED_CAST")
        return EMPTY as ValidationResults<T>
      }

      private val EMPTY = object : ValidationResults<Enum<*>>() {
        override fun registerProblems(problemType: Enum<*>, holder: ProblemsHolder) {}
      }
    }
  }
}
