package com.intellij.dts.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.dts.DtsBundle
import com.intellij.dts.DtsIcons
import com.intellij.dts.documentation.DtsBundledBindings
import com.intellij.dts.lang.symbols.DtsDocumentationSymbol
import com.intellij.dts.documentation.DtsNodeBindingDocumentationTarget
import com.intellij.dts.documentation.DtsPropertyBindingDocumentationTarget
import com.intellij.dts.lang.psi.*
import com.intellij.dts.util.DtsTreeUtil
import com.intellij.dts.zephyr.DtsZephyrBindingProvider
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.elementType

class DtsCompletionContributor : CompletionContributor() {
    private fun addPropertyVariants(node: DtsNode, result: CompletionResultSet) {
        val binding = DtsZephyrBindingProvider.bindingFor(node) ?: return

        for (property in binding.properties.values) {
            val symbol = DtsDocumentationSymbol.from(DtsPropertyBindingDocumentationTarget(node.project, property))

            val lookup = LookupElementBuilder.create(symbol, property.name)
                .withTypeText(property.type.typeName)
                .withIcon(DtsIcons.Property)

            result.addElement(lookup)
        }
    }

    private fun addSubNodeVariants(node: DtsNode, result: CompletionResultSet) {
        if (node.isDtsRootNode()) {
            val provider = DtsZephyrBindingProvider.of(node.project)

            for (binding in DtsBundledBindings.entries) {
                val build = binding.build(provider) ?: continue

                val symbol = DtsDocumentationSymbol.from(DtsNodeBindingDocumentationTarget(
                    node.project,
                    binding.nodeName,
                    build,
                ))

                val lookup = LookupElementBuilder.create(symbol, binding.nodeName)
                    .withTypeText(DtsBundle.message("documentation.node_type"))
                    .withIcon(DtsIcons.Node)

                result.addElement(lookup)
            }
        }
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val name = parameters.position
        if (name.elementType != DtsTypes.NAME) return

        val propertyParent = when (val parent = name.parent) {
            is DtsProperty -> DtsTreeUtil.parentNode(parent)
            is PsiErrorElement -> parent.parent.parent as? DtsNode
            else -> null
        }

        val nodeParent = when (val parent = name.parent) {
            is DtsSubNode -> DtsTreeUtil.parentNode(parent)
            is PsiErrorElement -> parent.parent.parent as? DtsNode
            else -> null
        }

        // include special prefixes, like: #
        val set = result.withPrefixMatcher(name.text.removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED))

        propertyParent?.let { addPropertyVariants(it, set) }
        nodeParent?.let { addSubNodeVariants(it, set) }
    }
}