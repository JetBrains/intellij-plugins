package org.intellij.plugins.markdown.lang.references

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeaderImpl
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownLinkDestinationImpl

class MarkdownHeaderReference(linkDestinationImpl: MarkdownLinkDestinationImpl) : PsiPolyVariantReferenceBase<MarkdownLinkDestinationImpl>(
  linkDestinationImpl) {

  private val file: MarkdownFile = myElement.containingFile as MarkdownFile

  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    return getFileHeaders(file)
      .filter { header -> StringUtil.equalsIgnoreCase(anchorToHeaderConvertedText(myElement), header.presentation.locationString) }
      .map { header -> PsiElementResolveResult(header) }
      .toTypedArray()
  }

  override fun getVariants(): Array<MyLookupElement> {
    return getFileHeaders(file)
      .map { header -> MyLookupElement(headerToAnchorConvertedText(header)) }
      .toTypedArray()
  }

  private fun anchorToHeaderConvertedText(linkDestination: MarkdownLinkDestinationImpl): String {
    var text = linkDestination.text
    if (text.length < 2) return text

    text = text.substring("#".length, text.length)
    return StringUtil.replace(text, "-", " ")
  }

  private fun headerToAnchorConvertedText(header: MarkdownHeaderImpl): String {
    return "#" + StringUtil.replace(header.presentation.locationString ?: "", " ", "-").toLowerCase()
  }

  class MyLookupElement(private val myValue: String) : LookupElement() {
    override fun getLookupString(): String = myValue
  }

  companion object {
    fun getFileHeaders(file: MarkdownFile): List<MarkdownHeaderImpl> {
      return file.headers.filterIsInstance(MarkdownHeaderImpl::class.java)
    }
  }
}