package org.angular2.library.forms

import com.intellij.icons.AllIcons
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItemCustomizer
import com.intellij.polySymbols.context.PolyContext
import com.intellij.psi.PsiElement

class Angular2FormsCodeCompletionItemCustomizer : PolySymbolCodeCompletionItemCustomizer {

  override fun customize(
    item: PolySymbolCodeCompletionItem,
    context: PolyContext,
    kind: PolySymbolKind,
    location: PsiElement,
  ): PolySymbolCodeCompletionItem? =
    if (item.symbol?.kind in NG_FORM_ANY_CONTROL_PROPS)
      item.withIcon(AllIcons.Nodes.Property)
        .withTypeText(when (item.symbol?.kind) {
                        NG_FORM_CONTROL_PROPS -> FORM_CONTROL_CONSTRUCTOR
                        NG_FORM_GROUP_PROPS -> FORM_GROUP_CONSTRUCTOR
                        NG_FORM_ARRAY_PROPS -> FORM_ARRAY_CONSTRUCTOR
                        else -> null
                      })
    else
      item
}