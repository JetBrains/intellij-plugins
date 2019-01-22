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
import java.util.List;
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
  public List<Angular2Declaration> getDeclarations() {
    return getResolvedModuleScope().myDeclarations;
  }

  @NotNull
  public List<Angular2Module> getImports() {
    return getResolvedModuleScope().myImports;
  }

  @NotNull
  public List<Angular2Entity> getExports() {
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
    Pair<List<Angular2Declaration>, Boolean> declarations = symbolCollector.collect(source, DECLARATIONS_PROP, Angular2Declaration.class);
    Pair<List<Angular2Module>, Boolean> imports = symbolCollector.collect(source, IMPORTS_PROP, Angular2Module.class);
    return new ResolvedModuleScope(declarations.first, imports.first,
                                   declarations.second == Boolean.TRUE
                                   && imports.second == Boolean.TRUE
                                   && ContainerUtil.find(imports.first, module -> !module.areExportsFullyResolved()) == null);
  }

  private static <T> ResolvedModuleExports resolveModuleExports(@NotNull T source, SymbolCollector<T> symbolCollector) {
    Pair<List<Angular2Entity>, Boolean> exports = symbolCollector.collect(source, EXPORTS_PROP, Angular2Entity.class);
    return new ResolvedModuleExports(exports.first,
                                     exports.second == Boolean.TRUE);
  }

  public interface SymbolCollector<T> {
    <U extends Angular2Entity> Pair<List<U>, Boolean> collect(@NotNull T source,
                                                              @NotNull String propertyName,
                                                              @NotNull Class<U> symbolClazz);
  }

  private static class ResolvedModuleScope {
    final List<Angular2Declaration> myDeclarations;
    final List<Angular2Module> myImports;
    final boolean myIsScopeFullyResolved;

    private ResolvedModuleScope(List<Angular2Declaration> declarations,
                                List<Angular2Module> imports,
                                boolean isScopeFullyResolved) {
      myDeclarations = Collections.unmodifiableList(declarations);
      myImports = Collections.unmodifiableList(imports);
      myIsScopeFullyResolved = isScopeFullyResolved;
    }
  }

  private static class ResolvedModuleExports {
    final List<Angular2Entity> myExports;
    final boolean myAreExportsFullyResolved;

    private ResolvedModuleExports(List<Angular2Entity> exports,
                                  boolean areExportsFullyResolved) {
      myExports = Collections.unmodifiableList(exports);
      myAreExportsFullyResolved = areExportsFullyResolved;
    }
  }
}
