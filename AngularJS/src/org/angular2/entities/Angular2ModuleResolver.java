// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.Stack;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.entities.Angular2EntityUtils.forEachEntity;

public class Angular2ModuleResolver<T extends PsiElement> {

  @NonNls public static final String NG_MODULE_PROP = "ngModule";

  /**
   * @see <a href="https://angular.io/api/core/ModuleWithProviders">ModuleWithProviders</a>
   */
  @NonNls public static final String MODULE_WITH_PROVIDERS_CLASS = "ModuleWithProviders";

  @NonNls private static final String KEYS_PREFIX = "angular.moduleResolver.";
  private static final Key<CachedValue<ResolvedEntitiesList<Angular2Declaration>>> DECLARATIONS_KEY =
    new Key<>(KEYS_PREFIX + DECLARATIONS_PROP);
  private static final Key<CachedValue<ResolvedEntitiesList<Angular2Module>>> IMPORTS_KEY =
    new Key<>(KEYS_PREFIX + IMPORTS_PROP);
  private static final Key<CachedValue<ResolvedEntitiesList<Angular2Entity>>> EXPORTS_KEY =
    new Key<>(KEYS_PREFIX + EXPORTS_PROP);

  private final Supplier<? extends T> mySourceSupplier;
  private final SymbolCollector<T> mySymbolCollector;

  public Angular2ModuleResolver(@NotNull Supplier<? extends T> sourceSupplier,
                                @NotNull SymbolCollector<T> symbolCollector) {
    mySourceSupplier = sourceSupplier;
    mySymbolCollector = symbolCollector;
  }

  public @NotNull Set<Angular2Declaration> getDeclarations() {
    return getResolvedModuleList(DECLARATIONS_KEY, Angular2Declaration.class).entities;
  }

  public @NotNull Set<Angular2Module> getImports() {
    return getResolvedModuleList(IMPORTS_KEY, Angular2Module.class).entities;
  }

  public @NotNull Set<Angular2Entity> getExports() {
    return getResolvedModuleList(EXPORTS_KEY, Angular2Entity.class).entities;
  }

  public boolean isScopeFullyResolved() {
    if (!getResolvedModuleList(DECLARATIONS_KEY, Angular2Declaration.class).isFullyResolved) {
      return false;
    }
    ResolvedEntitiesList<Angular2Module> imports = getResolvedModuleList(IMPORTS_KEY, Angular2Module.class);
    return imports.isFullyResolved
           && ContainerUtil.and(imports.entities, m -> m.areExportsFullyResolved())
           && areExportsFullyResolved();
  }

  public boolean areExportsFullyResolved() {
    ResolvedEntitiesList<Angular2Entity> exports = getResolvedModuleList(EXPORTS_KEY, Angular2Entity.class);
    return exports.isFullyResolved &&
           ContainerUtil.and(exports.entities, e -> !(e instanceof Angular2Module) || ((Angular2Module)e).areExportsFullyResolved());
  }

  public boolean areDeclarationsFullyResolved() {
    return getResolvedModuleList(DECLARATIONS_KEY, Angular2Declaration.class).isFullyResolved;
  }

  public @NotNull Set<Angular2Declaration> getAllExportedDeclarations() {
    final T source = mySourceSupplier.get();
    return CachedValuesManager.getCachedValue(source, () -> {
      Set<Angular2Declaration> result = new HashSet<>();
      Angular2Module module = source instanceof Angular2Module
                              ? (Angular2Module)source
                              : Angular2EntitiesProvider.getModule(source);
      if (module != null) {
        Set<Angular2Module> processedModules = new HashSet<>();
        Stack<Angular2Module> moduleQueue = new Stack<>(module);
        while (!moduleQueue.empty()) {
          Angular2Module current = moduleQueue.pop();
          if (processedModules.add(current)) {
            forEachEntity(
              current.getExports(),
              m -> moduleQueue.push(m),
              declaration -> result.add(declaration)
            );
          }
        }
      }
      return Result.create(result, PsiModificationTracker.MODIFICATION_COUNT);
    });
  }

  private @NotNull <U extends Angular2Entity> ResolvedEntitiesList<U> getResolvedModuleList(@NotNull Key<CachedValue<ResolvedEntitiesList<U>>> key,
                                                                                            @NotNull Class<U> entityClass) {
    final T source = mySourceSupplier.get();
    final SymbolCollector<T> symbolCollector = mySymbolCollector;
    return CachedValuesManager.getCachedValue(source, key, () -> symbolCollector.collect(
      source,
      StringUtil.trimStart(key.toString(), KEYS_PREFIX),
      entityClass));
  }

  public interface SymbolCollector<T> {
    <U extends Angular2Entity> Result<ResolvedEntitiesList<U>> collect(@NotNull T source,
                                                                       @NotNull String propertyName,
                                                                       @NotNull Class<U> symbolClazz);
  }

  public static final class ResolvedEntitiesList<T extends Angular2Entity> {
    final Set<T> entities;
    final boolean isFullyResolved;

    private ResolvedEntitiesList(@NotNull Set<T> entities, boolean isFullyResolved) {
      this.entities = Collections.unmodifiableSet(entities);
      this.isFullyResolved = isFullyResolved;
    }

    public static <T extends Angular2Entity> Result<ResolvedEntitiesList<T>> createResult(@NotNull Set<T> entities,
                                                                                          boolean isFullyResolved,
                                                                                          @NotNull Object dependency) {
      return Result.createSingleDependency(new ResolvedEntitiesList<>(entities, isFullyResolved), dependency);
    }

    public static <T extends Angular2Entity> Result<ResolvedEntitiesList<T>> createResult(@NotNull Set<T> entities,
                                                                                          boolean isFullyResolved,
                                                                                          @NotNull Collection<?> dependencies) {
      return Result.create(new ResolvedEntitiesList<>(entities, isFullyResolved), dependencies);
    }
  }
}
