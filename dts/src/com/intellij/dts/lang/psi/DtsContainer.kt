package com.intellij.dts.lang.psi

import com.intellij.dts.lang.DtsAffiliation
import com.intellij.psi.PsiElement

interface DtsContainer : PsiElement {
    val dtsAffiliation: DtsAffiliation

    val isDtsRootContainer: Boolean

    val dtsEntries: List<DtsEntry>

    val dtsStatements: List<DtsStatement>
        get() = dtsEntries.map { it.dtsStatement }

    val dtsProperties: List<DtsProperty>
        get() = dtsStatements.filterIsInstance<DtsProperty>()

    val dtsNodes: List<DtsNode>
        get() = dtsStatements.filterIsInstance<DtsNode>()
}