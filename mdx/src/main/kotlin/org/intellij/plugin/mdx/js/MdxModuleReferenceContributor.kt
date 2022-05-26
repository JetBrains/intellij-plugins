package org.intellij.plugin.mdx.js

import com.intellij.javascript.JSModuleBaseReference
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.frameworks.amd.JSModuleReference
import com.intellij.lang.javascript.frameworks.modules.JSBaseModuleReferenceContributor
import com.intellij.lang.javascript.frameworks.modules.JSModuleFileReferenceSet
import com.intellij.lang.javascript.psi.resolve.JSModuleReferenceContributor
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import org.intellij.plugin.mdx.lang.psi.MdxFile
import org.jetbrains.annotations.NotNull


class MdxModuleReferenceContributor : JSBaseModuleReferenceContributor() {
    override fun isApplicable(host: PsiElement): Boolean {
        return !DialectDetector.isTypeScript(host)
    }

    override fun getReferences(unquotedRefText: String,
                               host: PsiElement,
                               offset: Int,
                               provider: PsiReferenceProvider?,
                               isCommonJS: Boolean): Array<out @NotNull FileReference> {
        if (!StringUtil.endsWith(unquotedRefText, ".mdx")) {
            return emptyArray()
        }
        val path = JSModuleReferenceContributor.getActualPath(unquotedRefText)
        val modulePath = path.second
        val resourcePathStartInd = path.first
        val index = resourcePathStartInd + offset
        val isSoft = isSoft(host, modulePath, isCommonJS)
        return getReferences(host, provider, modulePath, index, isSoft, null)

    }

    protected fun getReferences(host: PsiElement,
                                provider: PsiReferenceProvider?,
                                modulePath: String,
                                index: Int,
                                isSoft: Boolean,
                                templateName: String?): Array<out @NotNull FileReference> {

        return object : JSModuleFileReferenceSet(modulePath, host, index, provider, templateName) {


            override fun isSoft(): Boolean {
                return isSoft
            }

            override fun createFileReference(textRange: TextRange?, i: Int, text: String?): FileReference? {
                if (!StringUtil.endsWith(text!!, ".mdx")) {
                    return super.createFileReference(textRange, i, text)
                }

                return object : JSModuleReference(text, i, textRange!!, this, emptyArray(), templateName, isSoft) {
                    override fun innerResolve(caseSensitive: Boolean, containingFile: PsiFile): Array<ResolveResult> {
                        val result = super.innerResolve(caseSensitive, containingFile)
                        (result.indices).forEach { i ->
                            if (result[i].element is MdxFile) {
                                result[i] = PsiElementResolveResult((result[i].element as MdxFile).viewProvider.getPsi(MdxJSLanguage.INSTANCE))
                            }
                        }
                        return result

                    }
                }

            }
        }.allReferences
    }


    override fun getDefaultWeight(): Int {
        return JSModuleBaseReference.ModuleTypes.DEFAULT.weight().inc()
    }
}