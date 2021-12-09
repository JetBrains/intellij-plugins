// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.navigation;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveSelector;
import org.angular2.entities.Angular2DirectiveSelectorSymbol;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.index.Angular2SourceDirectiveIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.angular2.entities.Angular2EntitiesProvider.getDirective;

public class Angular2GotoSymbolContributor implements ChooseByNameContributorEx {

  @Override
  public void processNames(@NotNull Processor<? super String> processor,
                           @NotNull GlobalSearchScope scope,
                           @Nullable IdFilter filter) {
    StubIndex.getInstance().processAllKeys(Angular2SourceDirectiveIndex.KEY, key -> {
      if (Angular2EntityUtils.isElementDirectiveIndexName(key)) {
        key = Angular2EntityUtils.getElementName(key);
      }
      else if (Angular2EntityUtils.isAttributeDirectiveIndexName(key)) {
        key = Angular2EntityUtils.getAttributeName(key);
      }
      else {
        return true;
      }
      if (!key.isEmpty()) {
        return processor.process(key);
      }
      return true;
    }, scope, filter);
  }

  @Override
  public void processElementsWithName(@NotNull String name,
                                      @NotNull Processor<? super NavigationItem> processor,
                                      @NotNull FindSymbolParameters parameters) {
    Stream.of(Angular2EntityUtils.getAttributeDirectiveIndexName(name),
              Angular2EntityUtils.getElementDirectiveIndexName(name))
      .forEach(
        indexName -> StubIndex.getInstance().processElements(
          Angular2SourceDirectiveIndex.KEY, indexName, parameters.getProject(), parameters.getSearchScope(),
          parameters.getIdFilter(), JSImplicitElementProvider.class, provider -> {
            final JSElementIndexingData indexingData = provider.getIndexingData();
            if (indexingData != null) {
              final Collection<JSImplicitElement> elements = indexingData.getImplicitElements();
              if (elements != null) {
                for (JSImplicitElement element : elements) {
                  if (element.isValid()) {
                    Angular2Directive directive = getDirective(element);
                    if (directive != null) {
                      if (!processSelectors(name, directive.getSelector().getSimpleSelectorsWithPsi(), processor)) {
                        return false;
                      }
                      return true;
                    }
                  }
                }
              }
            }
            return true;
          }));
  }

  private static boolean processSelectors(@NotNull String name,
                                          @NotNull List<Angular2DirectiveSelector.SimpleSelectorWithPsi> selectors,
                                          @NotNull Processor<? super NavigationItem> processor) {

    for (Angular2DirectiveSelector.SimpleSelectorWithPsi selector : selectors) {
      if (!processSelectorElement(name, selector.getElement(), processor)) {
        return false;
      }
      for (Angular2DirectiveSelectorSymbol attribute : selector.getAttributes()) {
        if (!processSelectorElement(name, attribute, processor)) {
          return false;
        }
      }
      if (!processSelectors(name, selector.getNotSelectors(), processor)) {
        return false;
      }
    }
    return true;
  }

  private static boolean processSelectorElement(@NotNull String name,
                                                @Nullable Angular2DirectiveSelectorSymbol element,
                                                @NotNull Processor<? super NavigationItem> processor) {
    if (element == null   || !name.equals(element.getName())) return true;
    for (var target: element.getNavigationTargets(element.getProject())) {
      var navigatable = target.getNavigatable();
      if (navigatable instanceof NavigationItem
          && processor.process((NavigationItem)navigatable)) {
        return true;
      }
    }
    return false;
  }
}
