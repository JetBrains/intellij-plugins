package org.intellij.plugin.mdx.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.javascript.completion.JSCompletionContributor
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import gnu.trove.THashSet
import org.intellij.plugin.mdx.lang.psi.MdxFile
import org.intellij.plugins.markdown.lang.MarkdownElementTypes
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes

class MdxImportExportCompletionContributor : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(MarkdownTokenTypes.TEXT), object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(parameters: CompletionParameters,
                                        context: ProcessingContext,
                                        result: CompletionResultSet) {
                val node = parameters.position.node
                val nodeParent = node.treeParent
                if (nodeParent.psi.containingFile is MdxFile) {
                    if (nodeParent.elementType == MarkdownElementTypes.PARAGRAPH &&
                            (nodeParent.firstChildNode == node || nodeParent.text.trim().startsWith(node.text))) {
                        result.addElement(LookupElementBuilder.create("export ").bold())
                        result.addElement(LookupElementBuilder.create("import ").bold())
                    }
                }
            }
        })
    }
}