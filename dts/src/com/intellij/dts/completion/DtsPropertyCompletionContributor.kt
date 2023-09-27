package com.intellij.dts.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.DtsIcons
import com.intellij.dts.documentation.DtsPropertyBindingDocumentationTarget
import com.intellij.dts.documentation.DtsDocumentationSymbol
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.DtsProperty
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.dts.zephyr.DtsZephyrBindingProvider
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.elementType

class DtsPropertyCompletionContributor : CompletionContributor() {
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val name = parameters.position
        if (name.elementType != DtsTypes.NAME) return

        val node = when (val parent = name.parent) {
            is DtsProperty -> DtsTreeUtil.parentNode(parent)
            is PsiErrorElement -> parent.parent.parent as? DtsNode
            else -> null
        } ?: return

        val binding = DtsZephyrBindingProvider.bindingFor(node) ?: return

        // include special prefixes, like: #
        val set = result.withPrefixMatcher(name.text.removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED))

        for (property in binding.properties.values) {
            val symbol = DtsDocumentationSymbol.of(DtsPropertyBindingDocumentationTarget(node.project, property))

            val lookup = LookupElementBuilder.create(symbol, property.name)
                .withTypeText(property.type.typeName)
                .withIcon(DtsIcons.Property)

            set.addElement(lookup)
        }
   }
}