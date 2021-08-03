// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.tags;

import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.MultiMap;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2Directive;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

import static com.intellij.util.containers.ContainerUtil.filterIsInstance;
import static org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity.IN_SCOPE;
import static org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity.NOT_REACHABLE;

public final class Angular2XmlElementSourcesResolver {
  private final XmlTag myScope;
  private final Collection<?> mySources;
  private final NotNullLazyValue<Collection<PsiElement>> myDeclarations;

  public Angular2XmlElementSourcesResolver(@NotNull XmlTag scope,
                                           @NotNull Collection<?> sources,
                                           @NotNull Function<Angular2Directive, Collection<? extends PsiElement>> getProperties,
                                           @NotNull Function<Angular2Directive, Collection<? extends PsiElement>> getSelectors) {
    myScope = scope;
    mySources = new ArrayList<>(sources);
    myDeclarations = NotNullLazyValue.lazy(() -> buildDeclarations(getProperties, getSelectors));
  }

  public @NotNull XmlTag getScope() {
    return myScope;
  }

  public @NotNull Collection<?> getSources() {
    return mySources;
  }

  public @NotNull List<Angular2Directive> getSourceDirectives() {
    return filterIsInstance(mySources, Angular2Directive.class);
  }

  public @NotNull Collection<PsiElement> getDeclarations() {
    return myDeclarations.getValue();
  }

  public @NotNull Collection<PsiElement> buildDeclarations(@NotNull Function<Angular2Directive, Collection<? extends PsiElement>> getProperties,
                                                           @NotNull Function<Angular2Directive, Collection<? extends PsiElement>> getSelectors) {
    Set<PsiElement> result = new HashSet<>(getNonDirectiveElements());
    MultiMap<Angular2DeclarationsScope.DeclarationProximity, Angular2Directive> directivesByProximity = getDeclarationsByProximity();
    directivesByProximity.remove(NOT_REACHABLE);

    MultiMap<Angular2DeclarationsScope.DeclarationProximity, PsiElement> fieldsByProximity =
      mapValues(directivesByProximity, getProperties);
    if (!fieldsByProximity.get(IN_SCOPE).isEmpty()) {
      result.addAll(fieldsByProximity.get(IN_SCOPE));
      return result;
    }
    MultiMap<Angular2DeclarationsScope.DeclarationProximity, PsiElement> selectorsByProximity =
      mapValues(directivesByProximity, getSelectors);
    if (!selectorsByProximity.get(IN_SCOPE).isEmpty()) {
      result.addAll(selectorsByProximity.get(IN_SCOPE));
      return result;
    }

    if (!fieldsByProximity.isEmpty()) {
      result.addAll(fieldsByProximity.values());
    }
    else {
      result.addAll(selectorsByProximity.values());
    }
    return result;
  }


  private @NotNull List<PsiElement> getNonDirectiveElements() {
    List<PsiElement> result = new ArrayList<>(mySources.size());
    for (Object source : mySources) {
      if (source instanceof PsiElement && !(source instanceof Angular2Declaration)) {
        result.add((PsiElement)source);
      }
    }
    return result;
  }

  private @NotNull MultiMap<Angular2DeclarationsScope.DeclarationProximity, Angular2Directive> getDeclarationsByProximity() {
    MultiMap<Angular2DeclarationsScope.DeclarationProximity, Angular2Directive> result = new MultiMap<>();
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(myScope);
    for (Object source : mySources) {
      if (source instanceof Angular2Directive) {
        Angular2Directive directive = (Angular2Directive)source;
        result.putValue(scope.getDeclarationProximity(directive), directive);
      }
    }
    return result;
  }

  private static <K, V, T> MultiMap<K, T> mapValues(@NotNull MultiMap<K, ? extends V> source,
                                                    @NotNull Function<? super V, Collection<? extends T>> mapper) {
    MultiMap<K, T> result = MultiMap.createSet();
    source.entrySet().forEach(entry -> entry.getValue().forEach(
      value -> result.putValues(entry.getKey(), mapper.apply(value))));
    return result;
  }
}
