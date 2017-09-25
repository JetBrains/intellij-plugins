package org.intellij.plugins.markdown.lang.references

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.util.Processor
import com.intellij.util.containers.ContainerUtil
import org.intellij.plugins.markdown.MarkdownBundle
import org.intellij.plugins.markdown.lang.index.MarkdownHeadersIndex
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeaderImpl

class MarkdownAnchorReferenceImpl internal constructor(private val myAnchor: String,
                                                       private val myFileReference: FileReference?,
                                                       private val myPsiElement: PsiElement,
                                                       private val myOffset: Int) : MarkdownAnchorReference, PsiPolyVariantReferenceBase<PsiElement>(
  myPsiElement), EmptyResolveMessageProvider {
  private val file: PsiFile?
    get() = if (myFileReference != null) myFileReference.resolve() as? PsiFile else myPsiElement.containingFile.originalFile

  override fun getElement(): PsiElement = myPsiElement

  override fun getRangeInElement(): TextRange = TextRange(myOffset, myOffset + myAnchor.length)

  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    if (myAnchor.isEmpty()) return PsiElementResolveResult.createResults(myPsiElement)

    val project = myPsiElement.project

    // optimization: trying to find capitalized header
    val suggestedHeader = StringUtil.replace(canonicalText, "-", " ")
    var headers: Collection<PsiElement> = MarkdownHeadersIndex.collectFileHeaders(StringUtil.capitalize(suggestedHeader), project, file)
    if (headers.isNotEmpty()) return PsiElementResolveResult.createResults(headers)

    headers = MarkdownHeadersIndex.collectFileHeaders(StringUtil.capitalizeWords(suggestedHeader, true), project, file)
    if (headers.isNotEmpty()) return PsiElementResolveResult.createResults(headers)

    // header search
    headers = StubIndex.getInstance().getAllKeys(MarkdownHeadersIndex.KEY, project)
      .filter { Companion.dashed(it) == canonicalText }
      .flatMap { MarkdownHeadersIndex.collectFileHeaders(it, project, file) }

    return PsiElementResolveResult.createResults(headers)
  }

  override fun getCanonicalText(): String = myAnchor

  override fun getVariants(): Array<Any> {
    val project = myPsiElement.project
    val list = ContainerUtil.newArrayList<String>()

    StubIndex.getInstance().getAllKeys(MarkdownHeadersIndex.KEY, project)
      .forEach { key ->
        StubIndex.getInstance().processElements(MarkdownHeadersIndex.KEY, key, project,
                                                file?.let { GlobalSearchScope.fileScope(it) },
                                                MarkdownHeaderImpl::class.java,
                                                Processor { list.add(dashed(key)) }
        )
      }

    return list.toTypedArray()
  }

  override fun getUnresolvedMessagePattern(): String = if (file == null)
    MarkdownBundle.message("markdown.cannot.resolve.anchor.error.message", myAnchor)
  else
    MarkdownBundle.message("markdown.cannot.resolve.anchor.in.file.error.message", myAnchor, (file as PsiFile).name)

  companion object {
    private fun dashed(it: String) = it.toLowerCase().replace(" ", "-")
  }
}
