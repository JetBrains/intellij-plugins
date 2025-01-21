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
    if (item.symbol?.qualifiedKind in NG_FORM_ANY_CONTROL_PROPS)
      item.withIcon(AllIcons.Nodes.Property)
        .withTypeText(when (item.symbol?.qualifiedKind) {
          NG_FORM_CONTROL_PROPS -> FORM_CONTROL_CONSTRUCTOR
          NG_FORM_GROUP_PROPS -> FORM_GROUP_CONSTRUCTOR
          NG_FORM_ARRAY_PROPS -> FORM_ARRAY_CONSTRUCTOR
          else -> null
        })
    else
      item
}