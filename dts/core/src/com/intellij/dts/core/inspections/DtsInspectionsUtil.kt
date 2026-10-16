package com.intellij.dts.core.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.core.DtsBundle
import com.intellij.dts.core.lang.psi.DtsProperty
import com.intellij.dts.core.lang.psi.DtsStatement
import com.intellij.dts.core.lang.psi.getDtsAnnotationTarget
import com.intellij.dts.core.util.relativeTo
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import org.jetbrains.annotations.PropertyKey

// similar to strspn for checks.c
fun firstNotMatching(str: String, rx: Regex, callback: (Char) -> Unit): Boolean {
  for (char in str.toCharArray()) {
    if (!rx.matches(char.toString())) {
      callback(char)
      return true
    }
  }

  return false
}

fun propertyValueRange(property: DtsProperty): TextRange? {
  val values = property.dtsValues
  if (values.isEmpty()) return null

  val valuesRange = TextRange(values.first().startOffset, values.last().endOffset)
  return valuesRange.relativeTo(property.textRange)
}

private fun elementInspectionRange(element: PsiElement): TextRange? {
  if (element !is DtsStatement) return null

  return element.getDtsAnnotationTarget().textRange.relativeTo(element.textRange)
}

fun ProblemsHolder.registerProblem(
  element: PsiElement,
  bundleKey: @PropertyKey(resourceBundle = DtsBundle.BUNDLE) String,
  bundleParam: Any? = null,
  rangeInElement: TextRange? = null,
  fix: LocalQuickFix? = null,
) {
  val manager = InspectionManager.getInstance(element.project)
  val params = bundleParam?.let { arrayOf(it) } ?: emptyArray<Any>()
  val fixes = fix?.let { arrayOf(it) } ?: emptyArray()

  val descriptor = manager.createProblemDescriptor(
    element,
    rangeInElement ?: elementInspectionRange(element),
    DtsBundle.message(bundleKey, *params),
    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    isOnTheFly,
    *fixes,
  )

  registerProblem(descriptor)
}
