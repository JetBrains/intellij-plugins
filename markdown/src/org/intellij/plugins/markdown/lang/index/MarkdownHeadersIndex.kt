package org.intellij.plugins.markdown.lang.index

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StringStubIndexExtension
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.CommonProcessors
import com.intellij.util.containers.ContainerUtil
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeaderImpl

class MarkdownHeadersIndex : StringStubIndexExtension<MarkdownHeaderImpl>() {
  override fun getKey(): StubIndexKey<String, MarkdownHeaderImpl> = KEY

  companion object {
    val KEY = StubIndexKey.createIndexKey<String, MarkdownHeaderImpl>("markdown.header")

    fun collectFileHeaders(suggestHeaderRef: String, project: Project, psiFile: PsiFile?): Collection<PsiElement> {
      val list = ContainerUtil.newArrayList<PsiElement>()
      StubIndex.getInstance().processElements(MarkdownHeadersIndex.KEY, suggestHeaderRef, project,
                                              psiFile?.let { GlobalSearchScope.fileScope(it) },
                                              MarkdownHeaderImpl::class.java,
                                              CommonProcessors.CollectProcessor(list))
      return list
    }
  }
}
