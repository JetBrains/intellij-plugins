package org.jetbrains.vuejs

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptResolveScopeProvider
import com.intellij.lang.javascript.psi.resolve.JSElementResolveScopeProvider
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope

class VueElementResolveScopeProvider : JSElementResolveScopeProvider {
  private val tsProvider = object: TypeScriptResolveScopeProvider() {
    override fun isApplicable(file: VirtualFile): Boolean = true

    override fun restrictByFileType(file: VirtualFile,
                                    libraryService: TypeScriptLibraryProvider,
                                    moduleAndLibraryScope: GlobalSearchScope): GlobalSearchScope {
      return super.restrictByFileType(file, libraryService, moduleAndLibraryScope).
        uniteWith(GlobalSearchScope.getScopeRestrictedByFileTypes(moduleAndLibraryScope, VueFileType.INSTANCE))
    }
  }

  override fun getElementResolveScope(element: PsiElement): GlobalSearchScope? {
    val psiFile = element.containingFile
    if (psiFile?.fileType != VueFileType.INSTANCE) return null
    if (DialectDetector.isTypeScript(element)) {
      return tsProvider.getResolveScope(psiFile.viewProvider.virtualFile, element.project)
    }
    return null
  }
}