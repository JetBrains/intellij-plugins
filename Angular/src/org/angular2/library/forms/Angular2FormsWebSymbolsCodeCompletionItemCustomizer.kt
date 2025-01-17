package org.angular2.library.forms

import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.webSymbols.FrameworkId
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItemCustomizer
import com.intellij.webSymbols.utils.qualifiedKind

class Angular2FormsWebSymbolsCodeCompletionItemCustomizer : WebSymbolCodeCompletionItemCustomizer {

  override fun customize(
    item: WebSymbolCodeCompletionItem,
    framework: FrameworkId?,
    qualifiedKind: WebSymbolQualifiedKind,
    location: PsiElement,
  ): WebSymbolCodeCompletionItem? =
    if (qualifiedKind == NG_FORM_CONTROL_PROPS || qualifiedKind == NG_FORM_GROUP_PROPS
        || item.symbol?.qualifiedKind?.let { it == NG_FORM_CONTROL_PROPS || it == NG_FORM_GROUP_PROPS } == true)
      item.withIcon(AllIcons.Nodes.Property)
    else
      item
}