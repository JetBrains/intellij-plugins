package com.intellij.protobuf.kotlin

import com.intellij.protobuf.ide.gutter.PbLanguageSpecificLineMarkerProvider
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty

internal class PbKotlinLineMarkerProvider : PbLanguageSpecificLineMarkerProvider() {
    override fun isAcceptableElement(element: PsiElement): Boolean {
        return element is KtClassOrObject || element is KtNamedFunction || element is KtProperty
    }
}
