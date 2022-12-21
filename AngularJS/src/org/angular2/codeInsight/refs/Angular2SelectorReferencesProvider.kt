// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.refs;

import com.intellij.webSymbols.WebSymbol;
import com.intellij.webSymbols.references.WebSymbolReference;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.model.Symbol;
import com.intellij.model.psi.PsiExternalReferenceHost;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.model.psi.PsiSymbolReferenceHints;
import com.intellij.model.psi.PsiSymbolReferenceProvider;
import com.intellij.model.search.SearchRequest;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2DirectiveSelector;
import org.angular2.entities.Angular2DirectiveSelector.SimpleSelectorWithPsi;
import org.angular2.entities.Angular2DirectiveSelectorSymbol;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.html.psi.Angular2HtmlNgContentSelector;
import org.angular2.web.Angular2Symbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static org.angular2.Angular2DecoratorUtil.*;

public abstract class Angular2SelectorReferencesProvider implements PsiSymbolReferenceProvider {

  public static class NgContentSelectorProvider extends Angular2SelectorReferencesProvider {

    @Override
    protected @Nullable Angular2DirectiveSelector getDirectiveSelector(PsiExternalReferenceHost element) {
      if (element instanceof Angular2HtmlNgContentSelector) {
        return ((Angular2HtmlNgContentSelector)element).getSelector();
      }
      return null;
    }
  }

  public static class NgDecoratorSelectorProvider extends Angular2SelectorReferencesProvider {

    @Override
    protected @Nullable Angular2DirectiveSelector getDirectiveSelector(PsiExternalReferenceHost element) {
      if (isLiteralInNgDecorator(element, SELECTOR_PROP, COMPONENT_DEC, DIRECTIVE_DEC)) {
        return doIfNotNull(Angular2EntitiesProvider.getDirective(PsiTreeUtil.getParentOfType(element, ES6Decorator.class)),
                           dir -> dir.getSelector());
      }
      return null;
    }
  }

  @Override
  public @NotNull Collection<? extends @NotNull SearchRequest> getSearchRequests(@NotNull Project project,
                                                                                 @NotNull Symbol target) {
    return Collections.emptyList();
  }

  @Override
  public @NotNull Collection<? extends @NotNull PsiSymbolReference> getReferences(@NotNull PsiExternalReferenceHost element,
                                                                                  @NotNull PsiSymbolReferenceHints hints) {
    Angular2DirectiveSelector directiveSelector = getDirectiveSelector(element);
    if (directiveSelector == null) {
      return Collections.emptyList();
    }
    List<PsiSymbolReference> result = new SmartList<>();
    Consumer<Angular2DirectiveSelectorSymbol> add = selector -> {
      if (!selector.isDeclaration()) {
        result.add(new HtmlSelectorReference(selector));
      }
    };
    var offsetInTheElement = hints.getOffsetInElement();
    if (offsetInTheElement >= 0) {
      for (SimpleSelectorWithPsi selector : directiveSelector.getSimpleSelectorsWithPsi()) {
        var found = selector.getElementAt(offsetInTheElement);
        if (found != null) {
          add.accept(found);
          return result;
        }
      }
      return Collections.emptyList();
    }

    for (SimpleSelectorWithPsi selector : directiveSelector.getSimpleSelectorsWithPsi()) {
      if (selector.getElement() != null) {
        add.accept(selector.getElement());
      }
      for (Angular2DirectiveSelectorSymbol attr : selector.getAttributes()) {
        add.accept(attr);
      }
      for (SimpleSelectorWithPsi notSelector : selector.getNotSelectors()) {
        for (Angular2DirectiveSelectorSymbol attr : notSelector.getAttributes()) {
          add.accept(attr);
        }
      }
    }
    return result;
  }

  protected abstract @Nullable Angular2DirectiveSelector getDirectiveSelector(PsiExternalReferenceHost element);

  private static final class HtmlSelectorReference implements WebSymbolReference {

    private final Angular2DirectiveSelectorSymbol mySelectorSymbol;

    private HtmlSelectorReference(@NotNull Angular2DirectiveSelectorSymbol symbol) {
      mySelectorSymbol = symbol;
    }

    @Override
    public @NotNull PsiElement getElement() {
      return mySelectorSymbol.getSource();
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
      return mySelectorSymbol.getTextRangeInSource();
    }

    @Override
    public @NotNull Collection<WebSymbol> resolveReference() {
      var symbols = mySelectorSymbol.getReferencedSymbols();
      var nonSelectorSymbols = ContainerUtil.filter(symbols, symbol -> !(symbol instanceof Angular2Symbol));
      if (!nonSelectorSymbols.isEmpty()) {
        return nonSelectorSymbols;
      }
      else {
        return symbols;
      }
    }
  }
}
