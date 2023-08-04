package com.intellij.dts.ide

import com.intellij.dts.lang.DtsLanguage
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsStatement
import com.intellij.dts.lang.psi.getDtsPresentableText
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider

class DtsBreadcrumbsProvider : BreadcrumbsProvider {
    override fun getLanguages(): Array<Language> = arrayOf(DtsLanguage)

    override fun acceptElement(element: PsiElement): Boolean = when (element) {
        is DtsNode, is DtsProperty -> true
        else -> false
    }

    override fun getElementInfo(element: PsiElement): String = when (element) {
        is DtsStatement -> element.getDtsPresentableText()
        else -> "unknown"
    }
}

