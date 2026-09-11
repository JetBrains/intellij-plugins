package com.intellij.protobuf.kotlin

import com.intellij.protobuf.ide.gutter.findProtoDefinitions
import com.intellij.protobuf.jvm.PbJavaGotoDeclarationHandler
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtOperationReferenceExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression

internal class PbKotlinProtoResolver {
    fun resolve(ref: KtReferenceExpression): Array<PsiElement>? {
        resolveThroughKotlinReferences(ref)?.let { return it }
        resolveUnresolvedPlusAssign(ref)?.let { return it }

        val nameReference = ref as? KtNameReferenceExpression ?: return null
        return resolveImportedClass(nameReference)
            ?: resolveBuilderMethod(nameReference)
            ?: resolveEnumConstant(nameReference)
            ?: resolveKotlinDslFactory(nameReference)
            ?: resolveKotlinDslField(nameReference)
    }

    private fun resolveThroughKotlinReferences(
        ref: KtReferenceExpression
    ): Array<PsiElement>? {
        for (reference in ref.references) {
            val resolved = reference.resolve() ?: continue

            protoTargetsForGeneratedKotlinElement(resolved)?.let { return it }
            protoTargets(resolved)?.let { return it }

            val navigationElement = resolved.navigationElement
            if (navigationElement !== resolved) {
                protoTargetsForGeneratedKotlinElement(navigationElement)?.let { return it }
                protoTargets(navigationElement)?.let { return it }
            }
        }

        return null
    }

    private fun resolveUnresolvedPlusAssign(
        ref: KtReferenceExpression
    ): Array<PsiElement>? {
        val operation = ref as? KtOperationReferenceExpression ?: return null
        if (operation.text != "+=") return null

        val receiver = (operation.parent as? KtBinaryExpression)?.left ?: return null
        for (reference in receiver.references) {
            val resolved = reference.resolve() ?: continue

            protoTargetsForGeneratedKotlinElement(resolved)?.let { return it }

            val navigationElement = resolved.navigationElement
            if (navigationElement !== resolved) {
                protoTargetsForGeneratedKotlinElement(navigationElement)?.let { return it }
            }
        }

        return null
    }

    private fun resolveImportedClass(
        ref: KtNameReferenceExpression
    ): Array<PsiElement>? {
        if (!PbKotlinReferenceClassifier.isGeneratedClassReference(ref)) {
            return null
        }

        val qualifiedExpression: KtExpression = (ref.parent as? KtDotQualifiedExpression)
            ?.takeIf { it.selectorExpression == ref }
            ?: ref
        val psiClass = findQualifiedClass(ref, qualifiedExpression) ?: return null
        return protoTargets(psiClass)
    }

    private fun resolveBuilderMethod(
        ref: KtNameReferenceExpression
    ): Array<PsiElement>? {
        val methodName = ref.getReferencedName()

        if (!PbKotlinReferenceClassifier.isBuilderAccessorCall(ref)) {
            return null
        }

        val call = PsiTreeUtil.getParentOfType(
            ref,
            KtCallExpression::class.java,
            false
        ) ?: return null

        val callee = call.calleeExpression ?: return null
        if (callee != ref && !PsiTreeUtil.isAncestor(callee, ref, false)) {
            return null
        }

        val dot = call.parent as? KtDotQualifiedExpression ?: return null
        val receiverRootName = rootName(dot.receiverExpression) ?: return null

        val messageClass = findImportedClass(ref, receiverRootName) ?: return null
        val builderClass = messageClass.findInnerClassByName("Builder", false) ?: return null
        val method = builderClass.findMethodsByName(methodName, false).firstOrNull() ?: return null

        return protoTargets(method)
    }

    private fun resolveEnumConstant(
        ref: KtNameReferenceExpression
    ): Array<PsiElement>? {
        val enumValueName = ref.getReferencedName()

        val dot = ref.parent as? KtDotQualifiedExpression ?: return null
        val enumClass = findQualifiedClass(ref, dot.receiverExpression) ?: return null
        val field = enumClass.findFieldByName(enumValueName, false) ?: return null

        return protoTargets(field)
    }

    private fun protoTargets(element: PsiElement): Array<PsiElement>? {
        return PbJavaGotoDeclarationHandler
            .findProtoDeclarationForResolvedJavaElement(element)
            ?.takeIf { it.isNotEmpty() }
    }

    private fun protoTargetsForGeneratedKotlinElement(element: PsiElement): Array<PsiElement>? {
        val generatedDslTargets = PbKotlinGeneratedCodeSearch
            .protoTargetsForGeneratedKotlinElement(element)
            .toList()
        if (generatedDslTargets.isNotEmpty()) {
            return generatedDslTargets.toTypedArray()
        }

        return findProtoDefinitions(element)
            .toList()
            .takeIf { it.isNotEmpty() }
            ?.toTypedArray()
    }

    private fun findImportedClass(
        ref: KtNameReferenceExpression,
        shortName: String
    ): PsiClass? {
        val fqName = findImportedFqName(ref, shortName) ?: return null

        return JavaPsiFacade
            .getInstance(ref.project)
            .findClass(fqName, GlobalSearchScope.allScope(ref.project))
    }

    private fun findQualifiedClass(
        ref: KtNameReferenceExpression,
        expression: KtExpression
    ): PsiClass? {
        val classNames = qualifiedNameSegments(expression) ?: return null
        var psiClass = findImportedClass(ref, classNames.first()) ?: return null

        for (className in classNames.drop(1)) {
            psiClass = psiClass.findInnerClassByName(className, false) ?: return null
        }

        return psiClass
    }

    private fun qualifiedNameSegments(expression: KtExpression?): List<String>? {
        return when (expression) {
            is KtNameReferenceExpression -> listOf(expression.getReferencedName())
            is KtDotQualifiedExpression -> {
                val receiverNames = qualifiedNameSegments(expression.receiverExpression) ?: return null
                val selectorName = (expression.selectorExpression as? KtNameReferenceExpression)
                    ?.getReferencedName()
                    ?: return null
                receiverNames + selectorName
            }
            is KtParenthesizedExpression -> qualifiedNameSegments(expression.expression)
            else -> null
        }
    }

    private fun rootName(expression: KtExpression?): String? {
        return when (expression) {
            is KtNameReferenceExpression -> expression.getReferencedName()
            is KtDotQualifiedExpression -> rootName(expression.receiverExpression)
            is KtCallExpression -> rootName(expression.calleeExpression)
            is KtParenthesizedExpression -> rootName(expression.expression)
            else -> null
        }
    }

    private fun resolveKotlinDslFactory(
        ref: KtNameReferenceExpression
    ): Array<PsiElement>? {
        val functionName = ref.getReferencedName()

        if (!PbKotlinReferenceClassifier.isDslFactoryCall(ref)) {
            return null
        }

        val call = PsiTreeUtil.getParentOfType(
            ref,
            KtCallExpression::class.java,
            false
        ) ?: return null

        val callee = call.calleeExpression ?: return null
        if (callee != ref && !PsiTreeUtil.isAncestor(callee, ref, false)) {
            return null
        }

        val messageClass = findDslFactoryMessageClass(ref, functionName) ?: return null
        return protoTargets(messageClass)
    }

    private fun resolveKotlinDslField(
        ref: KtNameReferenceExpression
    ): Array<PsiElement>? {
        if (!PbKotlinReferenceClassifier.isDslFieldReference(ref)) {
            return null
        }

        val fieldName = ref.getReferencedName()

        val lambda = PsiTreeUtil.getParentOfType(
            ref,
            KtLambdaExpression::class.java,
            false
        ) ?: return null

        val factoryCall = findOwningCall(lambda) ?: return null
        val factoryName = (factoryCall.calleeExpression as? KtNameReferenceExpression)
            ?.getReferencedName()
            ?: return null

        val factoryReference = factoryCall.calleeExpression as? KtNameReferenceExpression ?: return null
        val messageClass = findDslFactoryMessageClass(factoryReference, factoryName) ?: return null
        val builderClass = messageClass.findInnerClassByName("Builder", false) ?: return null

        for (methodName in PbKotlinGeneratedNames.builderAccessorNamesForDslField(fieldName)) {
            val method = builderClass.findMethodsByName(methodName, false).firstOrNull()
                ?: continue

            val targets = protoTargets(method)
            if (!targets.isNullOrEmpty()) {
                return targets
            }
        }

        return null
    }

    private fun findDslFactoryMessageClass(
        ref: KtNameReferenceExpression,
        functionName: String
    ): PsiClass? {
        val messageClassName = PbKotlinGeneratedNames.messageClassNameForDslFactory(functionName)
        val importedFunctionFqName = findImportedFqName(ref, functionName)
        if (importedFunctionFqName != null) {
            val importedMessageClassName = PbKotlinGeneratedNames.messageClassNameForDslFactory(
                importedFunctionFqName.substringAfterLast(".")
            )
            val packageName = importedFunctionFqName.substringBeforeLast(
                delimiter = ".",
                missingDelimiterValue = ""
            )
            if (packageName.isNotEmpty()) {
                JavaPsiFacade
                    .getInstance(ref.project)
                    .findClass("$packageName.$importedMessageClassName", GlobalSearchScope.allScope(ref.project))
                    ?.let { return it }
            }
        }

        val enclosingLambda = PsiTreeUtil.getParentOfType(
            ref,
            KtLambdaExpression::class.java,
            false
        ) ?: return null
        val enclosingFactoryCall = findOwningCall(enclosingLambda) ?: return null
        val enclosingFactoryReference = enclosingFactoryCall.calleeExpression as? KtNameReferenceExpression
            ?: return null
        if (enclosingFactoryReference == ref) {
            return null
        }

        val enclosingMessageClass = findDslFactoryMessageClass(
            enclosingFactoryReference,
            enclosingFactoryReference.getReferencedName()
        ) ?: return null
        return enclosingMessageClass.findInnerClassByName(messageClassName, false)
    }

    private fun findImportedFqName(
        ref: KtNameReferenceExpression,
        shortName: String
    ): String? {
        val ktFile = ref.containingFile as? KtFile ?: return null

        for (importDirective in ktFile.importDirectives) {
            val importedFqName = importDirective.importedFqName?.asString() ?: continue

            val aliasName = importDirective.aliasName
            if (aliasName == shortName) {
                return importedFqName
            }

            if (importDirective.isAllUnder) {
                return "$importedFqName.$shortName"
            }

            if (importedFqName == shortName || importedFqName.endsWith(".$shortName")) {
                return importedFqName
            }
        }

        val packageName = ktFile.packageFqName.asString()
        return packageName
            .takeIf { it.isNotEmpty() }
            ?.let { "$it.$shortName" }
    }

    private fun findOwningCall(lambda: KtLambdaExpression): KtCallExpression? {
        var current: PsiElement? = lambda.parent

        while (current != null) {
            if (current is KtCallExpression) {
                return current
            }

            current = current.parent
        }

        return null
    }

}
