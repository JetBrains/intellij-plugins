package com.intellij.dts.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiRecursiveVisitor
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import kotlin.reflect.KClass

private fun createVisitor(recursive: Boolean, callback: (PsiElement) -> Unit): PsiElementVisitor {
    if (recursive) {
        return object : PsiElementVisitor(), PsiRecursiveVisitor {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                callback(element)

                element.acceptChildren(this)
            }
        }
    } else {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                callback(element)
            }
        }
    }
}

fun <T : PsiElement> dtsVisitor(clazz: KClass<T>, callback: (T) -> Unit) = createVisitor(false) {
    if (clazz.isInstance(it)) {
        @Suppress("UNCHECKED_CAST")
        callback(it as T)
    }
}

fun <T : PsiElement> dtsRecursiveVisitor(clazz: KClass<T>, callback: (T) -> Unit) = createVisitor(true) {
    if (clazz.isInstance(it)) {
        @Suppress("UNCHECKED_CAST")
        callback(it as T)
    }
}

fun dtsVisitor(type: IElementType, callback: (PsiElement) -> Unit) = createVisitor(false) {
    if (it.elementType == type) {
        callback(it)
    }
}
