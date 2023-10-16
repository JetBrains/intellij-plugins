package com.intellij.dts.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.dts.DtsBundle
import com.intellij.dts.lang.psi.DtsStatement
import com.intellij.dts.lang.psi.getDtsAnnotationTarget
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
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

fun ProblemsHolder.registerError(
    element: PsiElement,
    bundleKey: @PropertyKey(resourceBundle = DtsBundle.BUNDLE) String,
    bundleParam: Any? = null,
    rangeInElement: TextRange? = null,
    fix: LocalQuickFix? = null,
) = registerProblem(this, ProblemHighlightType.GENERIC_ERROR, element, bundleKey, bundleParam, rangeInElement, fix)

fun ProblemsHolder.registerWarning(
    element: PsiElement,
    bundleKey: @PropertyKey(resourceBundle = DtsBundle.BUNDLE) String,
    bundleParam: Any? = null,
    rangeInElement: TextRange? = null,
    fix: LocalQuickFix? = null,
) = registerProblem(this, ProblemHighlightType.WARNING, element, bundleKey, bundleParam, rangeInElement, fix)

private fun elementInspectionTarget(element: PsiElement): PsiElement {
    return if (element !is DtsStatement) {
        element
    } else {
        element.getDtsAnnotationTarget()
    }
}

private fun registerProblem(
    holder: ProblemsHolder,
    highlightType: ProblemHighlightType,
    element: PsiElement,
    bundleKey: @PropertyKey(resourceBundle = DtsBundle.BUNDLE) String,
    bundleParam: Any?,
    rangeInElement: TextRange?,
    fix: LocalQuickFix?
) {
    val manager = InspectionManager.getInstance(element.project)
    val params = bundleParam?.let { arrayOf(it) } ?: emptyArray<Any>()
    val fixes = fix?.let { arrayOf(it) } ?: emptyArray()

    val descriptor = manager.createProblemDescriptor(
        elementInspectionTarget(element),
        rangeInElement,
        DtsBundle.message(bundleKey, *params),
        highlightType,
        holder.isOnTheFly,
        *fixes,
    )

    holder.registerProblem(descriptor)
}