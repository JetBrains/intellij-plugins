package org.jetbrains.webstorm.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import org.jetbrains.webstorm.lang.psi.WebAssemblyTypes.*
import java.util.*


class WebAssemblyFoldingBuilder : FoldingBuilderEx(), DumbAware {
    override fun getPlaceholderText(node: ASTNode): String? = "..."

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> =
        addDescriptorsOfRoot(ArrayList(), root).toTypedArray()

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false

    private fun addDescriptorsOfRoot(descriptors: MutableList<FoldingDescriptor>, root: PsiElement):
            MutableList<FoldingDescriptor> {
        root.children.map {
            when (it.elementType) {
                MODULEFIELD -> {
                    addDescriptorsForNode(descriptors, it.firstChild.node)
                    if (it.firstChild.elementType in arrayOf(FUNC, GLOBAL, ELEM, DATA)) {
                        it.firstChild.children.map { x ->
                            addDescriptorsOfRoot(descriptors, x)
                        }
                    } else {}
                }

                in arrayOf(FOLDEINSTR, BLOCKINSTR) -> {
                    addDescriptorsForNode(descriptors, it.node)
                }

                MODULE -> {
                    addDescriptorsForNode(descriptors, it.node)
                    addDescriptorsOfRoot(descriptors, it)
                }

                else -> {}
            }
        }

        return descriptors
    }

    private fun addDescriptorsForNode(descriptors: MutableList<FoldingDescriptor>, node: ASTNode) {
        descriptors.add(FoldingDescriptor(
                node,
                TextRange(
                        node.firstChildNode.textRange.endOffset,
                        node.textRange.endOffset -
                               if (node.lastChildNode.elementType in arrayOf(RPAR, ENDKEY))
                                   node.lastChildNode.textRange.length else 0)))
    }
}