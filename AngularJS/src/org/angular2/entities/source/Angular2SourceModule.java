// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.util.AstLoadingFilter;
import com.intellij.util.containers.Stack;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2Module;
import org.angular2.entities.Angular2ModuleResolver;
import org.angular2.entities.Angular2ModuleResolver.ResolvedEntitiesList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.angular2.Angular2DecoratorUtil.getObjectLiteralInitializer;
import static org.angular2.Angular2DecoratorUtil.getReferencedObjectLiteralInitializer;

public class Angular2SourceModule extends Angular2SourceEntity implements Angular2Module {

  private final Angular2ModuleResolver<ES6Decorator> myModuleResolver = new Angular2ModuleResolver<>(
    this::getDecorator, Angular2SourceModule::collectSymbols);

  public Angular2SourceModule(@NotNull ES6Decorator decorator,
                              @NotNull JSImplicitElement implicitElement) {
    super(decorator, implicitElement);
  }

  @Override
  public @NotNull Set<Angular2Declaration> getDeclarations() {
    return myModuleResolver.getDeclarations();
  }

  @Override
  public @NotNull Set<Angular2Module> getImports() {
    return myModuleResolver.getImports();
  }

  @Override
  public @NotNull Set<Angular2Entity> getExports() {
    return myModuleResolver.getExports();
  }

  @Override
  public @NotNull Set<Angular2Declaration> getAllExportedDeclarations() {
    return myModuleResolver.getAllExportedDeclarations();
  }

  @Override
  public boolean isScopeFullyResolved() {
    return myModuleResolver.isScopeFullyResolved();
  }

  @Override
  public boolean areExportsFullyResolved() {
    return myModuleResolver.areExportsFullyResolved();
  }

  @Override
  public boolean areDeclarationsFullyResolved() {
    return myModuleResolver.areDeclarationsFullyResolved();
  }

  @Override
  public boolean isPublic() {
    //noinspection HardCodedStringLiteral
    return !getName().startsWith("ɵ");
  }

  private static <T extends Angular2Entity> Result<ResolvedEntitiesList<T>> collectSymbols(@NotNull ES6Decorator decorator,
                                                                                           @NotNull String propertyName,
                                                                                           @NotNull Class<T> symbolClazz) {
    JSObjectLiteralExpression initializer = getObjectLiteralInitializer(decorator);
    if (initializer == null) {
      initializer = getReferencedObjectLiteralInitializer(decorator);
    }
    JSProperty property = initializer != null ? initializer.findProperty(propertyName)
                                              : null;
    if (property == null) {
      return ResolvedEntitiesList.createResult(Collections.emptySet(), true, decorator);
    }
    return AstLoadingFilter.forceAllowTreeLoading(property.getContainingFile(), () ->
      new SourceSymbolCollector<>(symbolClazz, decorator).collect(property.getValue()));
  }

  private static class SourceSymbolCollector<T extends Angular2Entity> extends Angular2SourceEntityListProcessor<T> {

    private boolean myIsFullyResolved = true;
    private final Set<T> myResult = new HashSet<>();
    private final Set<PsiElement> myDependencies = new HashSet<>();
    private final Stack<PsiElement> myResolveQueue = new Stack<>();
    private final ES6Decorator myDecorator;

    SourceSymbolCollector(@NotNull Class<T> entityClass, @NotNull ES6Decorator decorator) {
      super(entityClass);
      myDecorator = decorator;
    }

    public Result<ResolvedEntitiesList<T>> collect(@Nullable JSExpression value) {
      if (value == null) {
        return ResolvedEntitiesList.createResult(myResult, false, myDecorator);
      }
      Set<PsiElement> visited = new HashSet<>();
      processCacheDependency(myDecorator);
      myResolveQueue.push(value);
      while (!myResolveQueue.empty()) {
        ProgressManager.checkCanceled();
        PsiElement element = myResolveQueue.pop();
        if (!visited.add(element)) {
          // Protect against cyclic references or visiting same thing several times
          continue;
        }
        processCacheDependency(element);
        List<PsiElement> children = resolve(element);
        if (children.isEmpty()) {
          element.accept(getResultsVisitor());
        }
        else {
          myResolveQueue.addAll(children);
        }
      }
      return ResolvedEntitiesList.createResult(myResult, myIsFullyResolved, myDependencies);
    }

    @Override
    protected void processCacheDependency(PsiElement element) {
      myDependencies.add(element);
    }

    @Override
    protected void processNonEntityClass(@NotNull JSClass aClass) {
      myIsFullyResolved = false;
    }

    @Override
    protected void processEntity(@NotNull T entity) {
      myResult.add(entity);
    }

    @Override
    protected void processAnyType() {
      myIsFullyResolved = false;
    }

    @Override
    protected void processAnyElement(JSElement node) {
      myIsFullyResolved = false;
    }
  }
}
