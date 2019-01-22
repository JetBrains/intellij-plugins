// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
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

  private final Supplier<T> mySourceSupplier;
  private final SymbolCollector<T> mySymbolCollector;

  public Angular2ModuleResolver(Supplier<T> sourceSupplier,
                                SymbolCollector<T> symbolCollector) {
    mySourceSupplier = sourceSupplier;
    mySymbolCollector = symbolCollector;
  }

  @NotNull
  public Set<Angular2Declaration> getDeclarations() {
    return getResolvedModuleScope().myDeclarations;
  }

  @NotNull
  public Set<Angular2Module> getImports() {
    return getResolvedModuleScope().myImports;
  }

  @NotNull
  public Set<Angular2Entity> getExports() {
    return getResolvedModuleExports().myExports;
  }

  public boolean isScopeFullyResolved() {
    return getResolvedModuleScope().myIsScopeFullyResolved;
  }

  public boolean areExportsFullyResolved() {
    return getResolvedModuleExports().myAreExportsFullyResolved;
  }

  @NotNull
  private ResolvedModuleScope getResolvedModuleScope() {
    final T source = mySourceSupplier.get();
    @SuppressWarnings("UnnecessaryLocalVariable") final SymbolCollector<T> symbolCollector = mySymbolCollector;
    return CachedValuesManager.getCachedValue(source, () -> {
      ResolvedModuleScope resolvedModule = resolveModuleScope(source, symbolCollector);
      return CachedValueProvider.Result.create(resolvedModule, PsiModificationTracker.MODIFICATION_COUNT);
    });
  }

  @NotNull
  private ResolvedModuleExports getResolvedModuleExports() {
    final T source = mySourceSupplier.get();
    @SuppressWarnings("UnnecessaryLocalVariable") final SymbolCollector<T> symbolCollector = mySymbolCollector;
    return CachedValuesManager.getCachedValue(source, () -> {
      ResolvedModuleExports resolvedModule = resolveModuleExports(source, symbolCollector);
      return CachedValueProvider.Result.create(resolvedModule, PsiModificationTracker.MODIFICATION_COUNT);
    });
  }

  private static <T> ResolvedModuleScope resolveModuleScope(@NotNull T source, SymbolCollector<T> symbolCollector) {
    Pair<Set<Angular2Declaration>, Boolean> declarations = symbolCollector.collect(source, DECLARATIONS_PROP, Angular2Declaration.class);
    Pair<Set<Angular2Module>, Boolean> imports = symbolCollector.collect(source, IMPORTS_PROP, Angular2Module.class);
    return new ResolvedModuleScope(declarations.first, imports.first,
                                   declarations.second == Boolean.TRUE
                                   && imports.second == Boolean.TRUE
                                   && ContainerUtil.find(imports.first, module -> !module.areExportsFullyResolved()) == null);
  }

  private static <T> ResolvedModuleExports resolveModuleExports(@NotNull T source, SymbolCollector<T> symbolCollector) {
    Pair<Set<Angular2Entity>, Boolean> exports = symbolCollector.collect(source, EXPORTS_PROP, Angular2Entity.class);
    return new ResolvedModuleExports(exports.first,
                                     exports.second == Boolean.TRUE);
  }

  public interface SymbolCollector<T> {
    <U extends Angular2Entity> Pair<Set<U>, Boolean> collect(@NotNull T source,
                                                              @NotNull String propertyName,
                                                              @NotNull Class<U> symbolClazz);
  }

  private static class ResolvedModuleScope {
    final Set<Angular2Declaration> myDeclarations;
    final Set<Angular2Module> myImports;
    final boolean myIsScopeFullyResolved;

    private ResolvedModuleScope(Set<Angular2Declaration> declarations,
                                Set<Angular2Module> imports,
                                boolean isScopeFullyResolved) {
      myDeclarations = Collections.unmodifiableSet(declarations);
      myImports = Collections.unmodifiableSet(imports);
      myIsScopeFullyResolved = isScopeFullyResolved;
    }
  }

  private static class ResolvedModuleExports {
    final Set<Angular2Entity> myExports;
    final boolean myAreExportsFullyResolved;

    private ResolvedModuleExports(Set<Angular2Entity> exports,
                                  boolean areExportsFullyResolved) {
      myExports = Collections.unmodifiableSet(exports);
      myAreExportsFullyResolved = areExportsFullyResolved;
    }
  }
}
