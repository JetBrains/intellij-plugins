package org.intellij.plugin.mdx.emmet

import com.intellij.codeInsight.template.CustomTemplateCallback
import com.intellij.codeInsight.template.emmet.XmlEmmetConstants
import com.intellij.codeInsight.template.emmet.ZenCodingUtil
import com.intellij.codeInsight.template.emmet.tokens.TemplateToken
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.lang.javascript.frameworks.jsx.JSXZenCodingGenerator
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlText
import org.intellij.plugin.mdx.js.MdxJSLanguage
import org.intellij.plugin.mdx.lang.template.MdxFileViewProvider

internal class MdxZenCodingGenerator : JSXZenCodingGenerator() {
  override fun isMyContext(callback: CustomTemplateCallback, wrapping: Boolean): Boolean {
    val viewProvider = callback.file.viewProvider as? MdxFileViewProvider ?: return false
    if (isAfterUnfinishedTagStart(callback)) return false
    val mdxJsFile = viewProvider.getPsi(MdxJSLanguage.INSTANCE) ?: return false
    val offset = callback.editor.caretModel.offset
    val context = PsiUtilCore.getElementAtOffset(mdxJsFile, (offset - 1).coerceAtLeast(0))
    // Reject when cursor is inside a JSX/XML tag start (e.g. "<inp<caret>")
    // but allow expansion in tag text content (e.g. "<ul>.item*3<caret></ul>")
    val insideTag = PsiTreeUtil.getParentOfType(context, XmlTag::class.java) != null
    val inXmlText = PsiTreeUtil.getParentOfType(context, XmlText::class.java) != null
    return !insideTag || inXmlText
  }

  /**
   * For a standalone abbreviation (e.g. `ul>.item*3`), the platform synthesizes the tag tree in the base
   * [org.intellij.plugin.mdx.lang.MdxLanguage], where JSX/XML tags live only in the embedded `MdxJS`
   * sub-tree — so it never finds the tag to apply selectors (e.g. `.item`) to and emits a bare `<li></li>`.
   * Re-render the token's tag through [MdxJSLanguage] instead, apply the attributes there, and rewrite the
   * token text so the base generator resolves it and produces `<li class="item"></li>`.
   */
  override fun generateTemplate(token: TemplateToken, hasChildren: Boolean, context: PsiElement): TemplateImpl {
    applyAttributesViaJsx(token, context)
    return super.generateTemplate(token, hasChildren, context)
  }

  private fun applyAttributesViaJsx(token: TemplateToken, context: PsiElement) {
    val attributes = token.attributes.toMutableMap()
    if (attributes.isEmpty()) return

    if ("class" in attributes) attributes.remove("className")
    if ("for" in attributes) attributes.remove("htmlFor")
    if ("innerHTML" in attributes) attributes.remove("dangerouslySetInnerHTML")

    val template = token.template ?: return
    val jsxFile = context.containingFile.viewProvider.getPsi(MdxJSLanguage.INSTANCE) ?: return

    // Re-parse the source template in the JSX dialect, where the XML tag is exposed at the top level.
    val dummyFile = PsiFileFactory.getInstance(context.project)
      .createFileFromText("dummy.html", MdxJSLanguage.INSTANCE, template.templateText, false, true)
    val tag = PsiTreeUtil.findChildOfType(dummyFile, XmlTag::class.java) ?: return

    for ((name, value) in attributes) {
      if (XmlEmmetConstants.DEFAULT_ATTRIBUTE_NAME == name) continue
      if (StringUtil.isEmpty(value)) {
        // Empty selector value (e.g. `input[name]`): expose it as an editable template variable.
        if (template.variables.none { it.name == name }) {
          template.addVariable(name, "", "", true)
        }
        tag.setAttribute(name, StringBuilder().append('$').append(name).append('$').toString())
      }
      else {
        tag.setAttribute(name, ZenCodingUtil.getValue(value, 0, 1, null))
      }
    }

    // Rewrite the token text through the JSX file so the base generator's `token.getXmlTag()` resolves it.
    token.setTemplateText(tag.containingFile.text, jsxFile)
  }

  private fun isAfterUnfinishedTagStart(callback: CustomTemplateCallback): Boolean {
    val text = callback.editor.document.charsSequence
    val offset = callback.editor.caretModel.offset.coerceIn(0, text.length)
    var current = offset - 1
    while (current >= 0) {
      when (text[current]) {
        '>' -> return false
        '<' -> return isTagNameStart(text.getOrNull(current + 1))
      }
      current--
    }
    return false
  }

  private fun isTagNameStart(char: Char?): Boolean {
    return char != null && (char.isLetter() || char == '_')
  }
}
