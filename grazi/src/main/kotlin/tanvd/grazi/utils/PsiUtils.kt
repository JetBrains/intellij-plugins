package tanvd.grazi.utils

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

inline fun <reified T : PsiElement> PsiElement.filterFor(filter: (T) -> Boolean = { true }): Collection<T>
        = PsiTreeUtil.collectElementsOfType(this, T::class.java).filter(filter)
