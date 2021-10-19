// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.model.Symbol;
import com.intellij.model.psi.PsiSymbolDeclaration;
import com.intellij.model.psi.PsiSymbolDeclarationProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.angular2.entities.Angular2DirectiveSelector;
import org.angular2.entities.Angular2DirectiveSelectorSymbol;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static org.angular2.Angular2DecoratorUtil.*;

public class Angular2SelectorDeclarationsProvider implements PsiSymbolDeclarationProvider {

  @Override
  public @NotNull Collection<? extends @NotNull PsiSymbolDeclaration> getDeclarations(@NotNull PsiElement element, int offsetInElement) {
    Angular2DirectiveSelector directiveSelector = null;
    if (element instanceof Angular2HtmlNgContentSelector) {
      directiveSelector = ((Angular2HtmlNgContentSelector)element).getSelector();
    }
    else if (isLiteralInNgDecorator(element, SELECTOR_PROP, COMPONENT_DEC, DIRECTIVE_DEC)) {
      directiveSelector = doIfNotNull(Angular2EntitiesProvider.getDirective(PsiTreeUtil.getParentOfType(element, ES6Decorator.class)),
                                      dir -> dir.getSelector());
    }
    if (directiveSelector == null) {
      return Collections.emptyList();
    }

    for (Angular2DirectiveSelector.SimpleSelectorWithPsi selector : directiveSelector.getSimpleSelectorsWithPsi()) {
      Angular2DirectiveSelectorSymbol selectorPart = selector.getElementAt(offsetInElement);
      if (selectorPart != null) {
        if (selectorPart.isDeclaration()) {
          return Collections.singleton(new Angular2SelectorDeclaration(selectorPart));
        }
        break;
      }
    }
    return Collections.emptyList();
  }

  private static class Angular2SelectorDeclaration implements PsiSymbolDeclaration {

    private final Angular2DirectiveSelectorSymbol mySymbol;

    private Angular2SelectorDeclaration(Angular2DirectiveSelectorSymbol symbol) {
      mySymbol = symbol;
    }

    @Override
    public @NotNull PsiElement getDeclaringElement() {
      return mySymbol.getSource();
    }

    @Override
    public @NotNull TextRange getRangeInDeclaringElement() {
      return mySymbol.getTextRangeInSource();
    }

    @Override
    public @NotNull Symbol getSymbol() {
      return mySymbol;
    }
  }
}
