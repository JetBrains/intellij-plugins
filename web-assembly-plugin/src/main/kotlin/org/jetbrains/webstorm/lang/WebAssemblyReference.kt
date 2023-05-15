package org.jetbrains.webstorm.lang

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.jetbrains.webstorm.lang.psi.*

class WebAssemblyReference(node: ASTNode,
                           idxNode: ASTNode)
    : PsiPolyVariantReference, PsiReferenceBase<PsiElement>(node.psi,
            TextRange(idxNode.textRange.startOffset - node.startOffset,
                      idxNode.textRange.endOffset - node.startOffset)) {

    private val ident: String = element.text.substring(super.getRangeInElement().startOffset,
                                                       super.getRangeInElement().endOffset)

    private var namedElements: Array<WebAssemblyNamedElement>? =
        PsiTreeUtil.getParentOfType(node.psi, WebAssemblyModulefield::class.java)?.let { parent ->
            when (node.elementType) {
                WebAssemblyTypes.TYPEUSE_TYPEREF -> WebAssemblyUtil.findModulefield(WebAssemblyTypes.TYPE, parent)
                WebAssemblyTypes.START -> WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.FUNC, parent)
                WebAssemblyTypes.ELEM -> WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.TABLE, parent)
                WebAssemblyTypes.ELEMLIST -> WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.FUNC, parent)
                WebAssemblyTypes.DATA -> WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.MEM, parent)
                WebAssemblyTypes.CALL_INSTR -> WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.FUNC, parent)
                WebAssemblyTypes.CALL_INDIRECT_INSTR -> WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.TABLE, parent)
                WebAssemblyTypes.REF_FUNC_INSTR -> WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.FUNC, parent)
                WebAssemblyTypes.LOCAL_INSTR -> WebAssemblyUtil.findParamsLocals(parent)
                WebAssemblyTypes.GLOBAL_INSTR -> WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.GLOBAL, parent)
                WebAssemblyTypes.TABLE_IDX_INSTR -> WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.TABLE, parent)
                WebAssemblyTypes.TABLE_COPY_INSTR -> WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.TABLE, parent)
                WebAssemblyTypes.ELEM_DROP_INSTR -> WebAssemblyUtil.findModulefield(WebAssemblyTypes.ELEM, parent)
                WebAssemblyTypes.MEMORY_IDX_INSTR -> WebAssemblyUtil.findModulefield(WebAssemblyTypes.DATA, parent)

                WebAssemblyTypes.EXPORT -> {
                    when (node.findChildByType(WebAssemblyTypes.EXPORTDESC)?.psi?.firstChild?.nextSibling?.elementType) {
                        WebAssemblyTypes.FUNCKEY   -> WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.FUNC, parent)
                        WebAssemblyTypes.TABLEKEY  -> WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.TABLE, parent)
                        WebAssemblyTypes.MEMORYKEY -> WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.MEM, parent)
                        WebAssemblyTypes.GLOBALKEY -> WebAssemblyUtil.findImportedModulefield(WebAssemblyTypes.GLOBAL, parent)
                        else                       -> null
                    }
                }

                else -> null
            }
        }

    constructor(node: ASTNode, idxNode: ASTNode, namedElements: Array<WebAssemblyNamedElement>?): this(node, idxNode) {
        this.namedElements = namedElements
    }

    override fun resolve(): PsiElement? {
        val resolveResults = multiResolve(false)
        return if (resolveResults.isNotEmpty()) resolveResults[0].element else null
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val results: MutableList<ResolveResult> = mutableListOf()

        ident.toIntOrNull()?.let { id ->
            namedElements?.let {
                if (namedElements!!.size > id) {
                    results.add(PsiElementResolveResult(namedElements!![id]))
                }
            }
            return results.toTypedArray()
        }

        namedElements?.forEach {
            if (it.nameIdentifier?.text == ident) {
                results.add(PsiElementResolveResult(it))
            }
        }
        return results.toTypedArray()
    }
}