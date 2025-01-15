// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.psi.PsiElement
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector

interface Angular2DirectiveSelector {

  val psiParent: PsiElement

  val text: String

  val simpleSelectors: List<Angular2DirectiveSimpleSelector>

  val simpleSelectorsWithPsi: List<SimpleSelectorWithPsi>

  fun getSymbolForElement(elementName: String): Angular2DirectiveSelectorSymbol

  interface SimpleSelectorWithPsi {

    val element: Angular2DirectiveSelectorSymbol?

    val attributes: List<Angular2DirectiveSelectorSymbol>

    val notSelectors: List<SimpleSelectorWithPsi>

    val allSymbols: List<Angular2DirectiveSelectorSymbol>

    fun getElementAt(offset: Int): Angular2DirectiveSelectorSymbol?
  }
}
