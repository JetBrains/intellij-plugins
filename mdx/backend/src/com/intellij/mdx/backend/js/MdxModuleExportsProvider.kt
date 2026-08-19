package com.intellij.mdx.backend.js

import com.intellij.lang.ecmascript6.resolve.JSModuleElementsProcessor
import com.intellij.lang.ecmascript6.resolve.JSModuleExportsProvider
import com.intellij.psi.PsiElement
import org.intellij.plugin.mdx.js.MdxJSLanguage
import org.intellij.plugin.mdx.lang.psi.MdxFile

/**
 * Bridges a resolved `.mdx` module's base [MdxFile] PSI to the embedded MdxJS module where its ESM
 * `export` declarations actually live, so `import { Foo } from './x.mdx'` resolves and completes them —
 * the same bridge Astro/Vue/Svelte provide for their embedded scripts.
 */
internal class MdxModuleExportsProvider : JSModuleExportsProvider {
  override fun processExports(scope: PsiElement, processor: JSModuleElementsProcessor, weak: Boolean): Boolean {
    return true
  }

  override fun getAdditionalScopes(scope: PsiElement, visited: MutableCollection<PsiElement>): Collection<PsiElement> {
    if (scope !is MdxFile) return emptyList()
    val mdxJsPsi = scope.viewProvider.getPsi(MdxJSLanguage.INSTANCE) ?: return emptyList()
    return listOf(mdxJsPsi)
  }
}
