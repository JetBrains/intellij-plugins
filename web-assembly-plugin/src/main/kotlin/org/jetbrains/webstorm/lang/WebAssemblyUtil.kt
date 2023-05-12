package org.jetbrains.webstorm.lang

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.jetbrains.webstorm.lang.psi.*

object WebAssemblyUtil {
    fun findParamsLocals(parent: PsiElement): Array<WebAssemblyNamedElement> {
        val result: MutableList<WebAssemblyNamedElement> = mutableListOf()

        parent.children.forEach {
            if (it.elementType == WebAssemblyTypes.PARAM ||
                    it.elementType == WebAssemblyTypes.LOCAL) {
                result.add(it as WebAssemblyNamedElement)
            }
            result.addAll(findParamsLocals(it))
        }

        return result.toTypedArray()
    }

    fun findModulefield(type: IElementType, parent: PsiElement): Array<WebAssemblyNamedElement> {
        val result: MutableList<WebAssemblyNamedElement> = mutableListOf()

        parent.parent.children.forEach {
            if (it.firstChild.elementType == type) {
                result.add(it.firstChild as WebAssemblyNamedElement)
            }
        }

        return result.toTypedArray()
    }

    fun findImportedModulefield(type: IElementType, parent: PsiElement): Array<WebAssemblyNamedElement> {
        val result: MutableList<WebAssemblyNamedElement> = findModulefield(type, parent).toMutableList()

        when (type) {
            WebAssemblyTypes.FUNC   -> WebAssemblyTypes.FUNCKEY
            WebAssemblyTypes.TABLE  -> WebAssemblyTypes.TABLEKEY
            WebAssemblyTypes.MEM    -> WebAssemblyTypes.MEMORYKEY
            WebAssemblyTypes.GLOBAL -> WebAssemblyTypes.GLOBALKEY
            else                    -> null
        }?.let { importType ->
            result.addAll(findModulefield(WebAssemblyTypes.IMPORT, parent)
                    .filter {
                        (it as WebAssemblyImport).importdesc?.firstChild?.nextSibling.elementType == importType
                    })
        }

        return result.toTypedArray()
    }

    fun findModules(project: Project): Array<WebAssemblyModule> {
        val result: MutableList<WebAssemblyModule> = mutableListOf()
        val virtualFiles = FileTypeIndex.getFiles(WebAssemblyFileType, GlobalSearchScope.allScope(project))

        for (virtualFile in virtualFiles) {
            PsiManager.getInstance(project).findFile(virtualFile!!)?.let {
                PsiTreeUtil.getChildrenOfType(it, WebAssemblyModule::class.java)
                        ?.forEach { module -> module?.let{ result.add(module) } }
            }
        }
        return result.toTypedArray()
    }
}