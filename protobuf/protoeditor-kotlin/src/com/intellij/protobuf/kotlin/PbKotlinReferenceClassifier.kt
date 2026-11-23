package com.intellij.protobuf.kotlin

import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

internal object PbKotlinReferenceClassifier {
    fun isGeneratedClassReference(ref: KtNameReferenceExpression): Boolean {
        return ref.getReferencedName().firstOrNull()?.isUpperCase() == true
    }

    fun isBuilderAccessorCall(ref: KtNameReferenceExpression): Boolean {
        val methodName = ref.getReferencedName()

        return isCallCallee(ref) &&
            (methodName.startsWith("set") ||
                methodName.startsWith("add") ||
                methodName.startsWith("get") ||
                methodName.startsWith("has") ||
                methodName.startsWith("clear"))
    }

    fun isDslFactoryCall(ref: KtNameReferenceExpression): Boolean {
        return ref.getReferencedName().firstOrNull()?.isLowerCase() == true &&
            isCallCallee(ref)
    }

    fun isDslFieldReference(ref: KtNameReferenceExpression): Boolean {
        return !isCallCallee(ref)
    }

    private fun isCallCallee(ref: KtNameReferenceExpression): Boolean {
        val call = PsiTreeUtil.getParentOfType(
            ref,
            KtCallExpression::class.java,
            false
        ) ?: return false

        val callee = call.calleeExpression ?: return false
        return callee == ref || PsiTreeUtil.isAncestor(callee, ref, false)
    }
}
