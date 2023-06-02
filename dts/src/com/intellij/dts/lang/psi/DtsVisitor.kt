package com.intellij.dts.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import kotlin.reflect.KClass

fun <T : PsiElement> dtsVisitor(clazz: KClass<T>, callback: (T) -> Unit): PsiElementVisitor {
    return object : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            super.visitElement(element)

            if (clazz.isInstance(element)) {
                @Suppress("UNCHECKED_CAST")
                callback(element as T)
            }
        }
    }
}

fun dtsVisitor(type: IElementType, callback: (PsiElement) -> Unit): PsiElementVisitor {
    return object : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            super.visitElement(element)

            if (element.elementType == type) {
                callback(element)
            }
        }
    }
}
