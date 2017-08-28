package org.intellij.plugins.markdown.lang.index

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.CommonProcessors
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeaderImpl
import java.util.*

class MarkdownHeadersIndex : StringStubIndexExtension<MarkdownHeaderImpl>() {
  override fun getKey(): StubIndexKey<String, MarkdownHeaderImpl> = KEY

  companion object {
    val KEY = StubIndexKey.createIndexKey<String, MarkdownHeaderImpl>("markdown.header")

    fun collectFileHeaders(suggestHeaderRef: String, project: Project, list: ArrayList<PsiElement>, psiFile: PsiFile?) {
      StubIndex.getInstance().processElements(MarkdownHeadersIndex.KEY, suggestHeaderRef, project,
                                              psiFile?.let { GlobalSearchScope.fileScope(it) },
                                              MarkdownHeaderImpl::class.java,
                                              CommonProcessors.CollectProcessor(list))
    }
  }
}
