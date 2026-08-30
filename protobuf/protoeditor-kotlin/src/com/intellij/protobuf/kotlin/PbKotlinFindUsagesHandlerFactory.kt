package com.intellij.protobuf.kotlin

import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesHandlerFactory
import com.intellij.protobuf.ide.gutter.findImplementations
import com.intellij.protobuf.lang.psi.PbField
import com.intellij.protobuf.lang.psi.PbMessageType
import com.intellij.protobuf.lang.psi.PbOneofDefinition
import com.intellij.protobuf.lang.psi.PbServiceDefinition
import com.intellij.protobuf.lang.psi.PbServiceMethod
import com.intellij.protobuf.lang.psi.PbSymbol
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Adds Kotlin generated DSL declarations to proto Find Usages without dropping Java generated
 * declarations supplied by the JVM module.
 */
class PbKotlinFindUsagesHandlerFactory : FindUsagesHandlerFactory() {
    override fun canFindUsages(psiElement: PsiElement): Boolean {
        return psiElement is PbSymbol
    }

    override fun createFindUsagesHandler(
        psiElement: PsiElement,
        forHighlightUsages: Boolean
    ): FindUsagesHandler? {
        if (forHighlightUsages) {
            return null
        }

        val symbol = psiElement as? PbSymbol ?: return null
        val secondaryElements = linkedSetOf<PsiElement>()

        secondaryElements.addAll(PbKotlinGeneratedCodeSearch.javaGeneratedElements(symbol))
        secondaryElements.addAll(kotlinGeneratedElements(symbol))

        if (secondaryElements.isEmpty()) {
            return null
        }

        return AdditionalUsagesHandler(symbol, secondaryElements.toTypedArray())
    }

    private fun kotlinGeneratedElements(symbol: PbSymbol): List<PsiElement> {
        return when (symbol) {
            is PbMessageType ->
                PbKotlinGeneratedCodeSearch.dslFactoryFunctions(symbol) +
                    PbKotlinGeneratedCodeSearch.dslCopyFunctions(symbol) +
                    PbKotlinGeneratedCodeSearch.dslExtensionFunctions(symbol)
            is PbField ->
                PbKotlinGeneratedCodeSearch.dslProperties(symbol) +
                    PbKotlinGeneratedCodeSearch.dslAccessorFunctions(symbol)
            is PbOneofDefinition ->
                PbKotlinGeneratedCodeSearch.dslOneofProperties(symbol) +
                    PbKotlinGeneratedCodeSearch.dslOneofAccessorFunctions(symbol)
            is PbServiceDefinition, is PbServiceMethod ->
                findImplementations(symbol)
                    .filter { implementation ->
                        implementation is KtClassOrObject || implementation is KtNamedFunction
                    }
                    .toList()
            else -> emptyList()
        }
    }

    private class AdditionalUsagesHandler(
        element: PbSymbol,
        private val additionalElements: Array<PsiElement>
    ) : FindUsagesHandler(element) {
        override fun getSecondaryElements(): Array<PsiElement> {
            return additionalElements
        }
    }
}
