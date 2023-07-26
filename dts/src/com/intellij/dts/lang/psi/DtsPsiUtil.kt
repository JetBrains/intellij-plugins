package com.intellij.dts.lang.psi

import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiElement

fun DtsNode.getDtsPresentableText(): @NlsSafe String {
    return when (this) {
        is DtsNode.Ref -> dtsHandle.text
        is DtsNode.Root -> "/"
        is DtsNode.Sub -> dtsName
    }
}

fun DtsStatement.getDtsPresentableText(): @NlsSafe String {
    return when (this) {
        is DtsStatement.CompilerDirective -> dtsDirective.text
        is DtsStatement.Node -> (this as DtsNode).getDtsPresentableText()
        is DtsStatement.Property -> dtsName
    }
}

fun DtsNode.getDtsAnnotationTarget(): PsiElement {
    return when (this) {
        is DtsNode.Ref -> dtsHandle
        is DtsNode.Root -> dtsSlash
        is DtsNode.Sub -> dtsNameElement
    }
}

fun DtsStatement.getDtsAnnotationTarget(): PsiElement {
    return when (this) {
        is DtsStatement.CompilerDirective -> dtsDirective
        is DtsStatement.Node -> (this as DtsNode).getDtsAnnotationTarget()
        is DtsStatement.Property -> dtsNameElement
    }
}

fun DtsNode.Ref.getDtsReferenceTarget(): DtsNode? {
    return dtsHandle.reference?.resolve() as? DtsNode
}