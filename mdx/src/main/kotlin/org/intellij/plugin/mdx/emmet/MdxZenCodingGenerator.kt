package org.intellij.plugin.mdx.emmet

import com.intellij.codeInsight.template.CustomTemplateCallback
import com.intellij.lang.javascript.frameworks.jsx.JSXZenCodingGenerator
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import org.intellij.plugin.mdx.js.MdxJSLanguage
import org.intellij.plugin.mdx.lang.psi.MdxFileViewProvider

internal class MdxZenCodingGenerator : JSXZenCodingGenerator() {
  override fun isMyContext(callback: CustomTemplateCallback, wrapping: Boolean): Boolean {
    val viewProvider = callback.file.viewProvider as? MdxFileViewProvider ?: return false
    val mdxJsFile = viewProvider.getPsi(MdxJSLanguage.INSTANCE) ?: return false
    val offset = callback.editor.caretModel.offset
    val context = mdxJsFile.findElementAt(offset) ?: mdxJsFile.findElementAt(offset - 1) ?: return true
    // Reject when cursor is inside a JSX/XML tag start (e.g. "<inp<caret>")
    // but allow expansion in tag text content (e.g. "<ul>.item*3<caret></ul>")
    val insideTag = PsiTreeUtil.getParentOfType(context, XmlTag::class.java) != null
    val inXmlText = PsiTreeUtil.getParentOfType(context, XmlText::class.java) != null
    return !insideTag || inXmlText
  }
}
