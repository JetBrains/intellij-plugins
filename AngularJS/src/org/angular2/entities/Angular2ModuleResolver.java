// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import static org.angular2.Angular2DecoratorUtil.*;

public class Angular2ModuleResolver<T extends PsiElement> {

  public static final String NG_MODULE_PROP = "ngModule";

  private static final String KEYS_PREFIX = "angular.moduleResolver.";
  private static final Key<CachedValue<ResolvedModuleList<Angular2Declaration>>> DECLARATIONS_KEY =
    new Key<>(KEYS_PREFIX + DECLARATIONS_PROP);
  private static final Key<CachedValue<ResolvedModuleList<Angular2Module>>> IMPORTS_KEY =
    new Key<>(KEYS_PREFIX + IMPORTS_PROP);
  private static final Key<CachedValue<ResolvedModuleList<Angular2Entity>>> EXPORTS_KEY =
    new Key<>(KEYS_PREFIX + EXPORTS_PROP);

  private final Supplier<T> mySourceSupplier;
  private final SymbolCollector<T> mySymbolCollector;

  public Angular2ModuleResolver(Supplier<T> sourceSupplier,
                                SymbolCollector<T> symbolCollector) {
    mySourceSupplier = sourceSupplier;
    mySymbolCollector = symbolCollector;
  }

  @NotNull
  public Set<Angular2Declaration> getDeclarations() {
    return getResolvedModuleList(DECLARATIONS_KEY, Angular2Declaration.class).list;
  }

  @NotNull
  public Set<Angular2Module> getImports() {
    return getResolvedModuleList(IMPORTS_KEY, Angular2Module.class).list;
  }

  @NotNull
  public Set<Angular2Entity> getExports() {
    return getResolvedModuleList(EXPORTS_KEY, Angular2Entity.class).list;
  }

  public boolean isScopeFullyResolved() {
    if (!getResolvedModuleList(DECLARATIONS_KEY, Angular2Declaration.class).isFullyResolved) {
      return false;
    }
    ResolvedModuleList<Angular2Module> imports = getResolvedModuleList(IMPORTS_KEY, Angular2Module.class);
    return imports.isFullyResolved
           && ContainerUtil.find(imports.list, m -> !m.areExportsFullyResolved()) == null;
  }

  public boolean areExportsFullyResolved() {
    ResolvedModuleList<Angular2Entity> exports = getResolvedModuleList(EXPORTS_KEY, Angular2Entity.class);
    return exports.isFullyResolved
           && ContainerUtil.find(exports.list, m -> m instanceof Angular2Module
                                                    && !((Angular2Module)m).areExportsFullyResolved()) == null;
  }

  public boolean areDeclarationsFullyResolved() {
    return getResolvedModuleList(DECLARATIONS_KEY, Angular2Declaration.class).isFullyResolved;
  }

  @NotNull
  private <U extends Angular2Entity> ResolvedModuleList<U> getResolvedModuleList(@NotNull Key<CachedValue<ResolvedModuleList<U>>> key,
                                                                                 @NotNull Class<U> entityClass) {
    final T source = mySourceSupplier.get();
    @SuppressWarnings("UnnecessaryLocalVariable") final SymbolCollector<T> symbolCollector = mySymbolCollector;
    return CachedValuesManager.getCachedValue(source, key, () ->
      CachedValueProvider.Result.create(new ResolvedModuleList<>(symbolCollector.collect(
        source,
        StringUtil.trimStart(key.toString(), KEYS_PREFIX),
        entityClass)), PsiModificationTracker.MODIFICATION_COUNT));
  }

  public interface SymbolCollector<T> {
    <U extends Angular2Entity> Pair<Set<U>, Boolean> collect(@NotNull T source,
                                                             @NotNull String propertyName,
                                                             @NotNull Class<U> symbolClazz);
  }

  private static class ResolvedModuleList<T extends Angular2Entity> {
    final Set<T> list;
    final boolean isFullyResolved;

    private ResolvedModuleList(@NotNull Pair<Set<T>, Boolean> resolutionResult) {
      list = Collections.unmodifiableSet(resolutionResult.first);
      isFullyResolved = resolutionResult.second == Boolean.TRUE;
    }
  }
}
