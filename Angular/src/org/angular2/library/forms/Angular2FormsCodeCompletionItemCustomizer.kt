package org.angular2.library.forms

import com.intellij.icons.AllIcons
import com.intellij.polySymbols.FrameworkId
import com.intellij.polySymbols.PolySymbolQualifiedKind
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItemCustomizer
import com.intellij.psi.PsiElement

class Angular2FormsCodeCompletionItemCustomizer : PolySymbolCodeCompletionItemCustomizer {

  override fun customize(
    item: PolySymbolCodeCompletionItem,
    framework: FrameworkId?,
    qualifiedKind: PolySymbolQualifiedKind,
    location: PsiElement,
  ): PolySymbolCodeCompletionItem? =
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