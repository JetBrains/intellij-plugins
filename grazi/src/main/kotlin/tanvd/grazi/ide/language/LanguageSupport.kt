// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package tanvd.grazi.ide.language

import com.intellij.lang.LanguageExtension
import com.intellij.lang.LanguageExtensionPoint
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil.isAncestor
import tanvd.grazi.grammar.Typo

abstract class LanguageSupport(private val disabledRules: Set<String> = emptySet()) : LanguageExtensionPoint<LanguageSupport>() {
    companion object : LanguageExtension<LanguageSupport>("tanvd.grazi.languageSupport")

    abstract fun isRelevant(element: PsiElement): Boolean

    fun getTypos(element: PsiElement): Set<Typo> {
        require(isRelevant(element)) { "Got not relevant element in LanguageSupport" }
        return check(element)
            .asSequence()
            .filterNot { it.info.rule.id in disabledRules }
            .filter {
                it.location.element?.let { gotElement -> isAncestor(element, gotElement, false) } ?: false
            }.toSet()
    }

    /** Don't forget to use ProgressManager.checkCancelled() */
    protected abstract fun check(element: PsiElement): Set<Typo>
}
