// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.Stack;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.Arrays.asList;
import static org.angular2.Angular2DecoratorUtil.*;

public class Angular2SourceModule extends Angular2SourceEntity implements Angular2Module {

  public Angular2SourceModule(@NotNull ES6Decorator decorator,
                              @NotNull JSImplicitElement implicitElement) {
    super(decorator, implicitElement);
  }

  @NotNull
  @Override
  public List<Angular2Declaration> getDeclarations() {
    return getResolvedModule().myDeclarations;
  }

  @NotNull
  @Override
  public List<Angular2Module> getImports() {
    return getResolvedModule().myImports;
  }

  @NotNull
  @Override
  public List<Angular2Entity> getExports() {
    return getResolvedModule().myExports;
  }

  @Override
  public boolean isScopeFullyResolved() {
    return getResolvedModule().myIsScopeFullyResolved;
  }

  @Override
  public boolean areExportsFullyResolved() {
    return getResolvedModule().myAreExportsFullyResolved;
  }

  private ResolvedModule getResolvedModule() {
    ES6Decorator decorator = getDecorator();
    return CachedValuesManager.getCachedValue(decorator, () -> {
      ResolvedModule resolvedModule = resolveModule(decorator);
      return CachedValueProvider.Result.create(resolvedModule, resolvedModule.myDependencies);
    });
  }

  private static ResolvedModule resolveModule(@NotNull ES6Decorator decorator) {
    Pair<List<Angular2Entity>, Boolean> exports = collectSymbols(decorator, EXPORTS_PROP, Angular2Entity.class);
    Pair<List<Angular2Declaration>, Boolean> declarations = collectSymbols(decorator, DECLARATIONS_PROP, Angular2Declaration.class);
    Pair<List<Angular2Module>, Boolean> imports = collectSymbols(decorator, IMPORTS_PROP, Angular2Module.class);
    return new ResolvedModule(exports.first, declarations.first, imports.first,
                              exports.second == Boolean.TRUE,
                              declarations.second == Boolean.TRUE
                              && imports.second == Boolean.TRUE
                              && ContainerUtil.find(imports.first, module -> !module.areExportsFullyResolved()) == null,
                              Collections.singletonList(PsiModificationTracker.MODIFICATION_COUNT));
  }

  private static <T extends Angular2Entity> Pair<List<T>, Boolean> collectSymbols(@NotNull ES6Decorator decorator,
                                                                                  @NotNull String propertyName,
                                                                                  @NotNull Class<T> symbolClazz) {
    JSProperty property = getProperty(decorator, propertyName);
    if (property == null) {
      return Pair.pair(Collections.emptyList(), true);
    }
    return new SymbolCollector<>(symbolClazz).collect(property.getValue());
  }

  private static class SymbolCollector<T extends Angular2Entity> extends JSElementVisitor {

    private final Stack<PsiElement> myResolveStack = new Stack<>();
    private final Class<T> mySymbolClazz;
    private final List<T> myResult = new ArrayList<>();
    private boolean myIsFullyResolved = true;

    SymbolCollector(@NotNull Class<T> symbolClazz) {
      mySymbolClazz = symbolClazz;
    }

    public Pair<List<T>, Boolean> collect(@Nullable JSExpression value) {
      if (value == null) {
        return Pair.pair(myResult, false);
      }
      myResolveStack.push(value);
      while (!myResolveStack.empty()) {
        myResolveStack.pop().accept(this);
      }
      return Pair.pair(myResult, myIsFullyResolved);
    }

    @Override
    public void visitElement(PsiElement element) {
      myIsFullyResolved = false;
    }

    @Override
    public void visitJSArrayLiteralExpression(JSArrayLiteralExpression node) {
      myResolveStack.addAll(asList(node.getExpressions()));
    }

    @Override
    public void visitJSReferenceExpression(JSReferenceExpression node) {
      push(node.resolve());
    }

    @Override
    public void visitJSVariable(JSVariable node) {
      // TODO try to use stub here
      push(node.getInitializer());
    }

    @Override
    public void visitJSClass(JSClass aClass) {
      myIsFullyResolved &= tryAddEntity(aClass);
    }

    @Override
    public void visitJSCallExpression(JSCallExpression node) {
      push(node.getStubSafeMethodExpression());
    }

    @Override
    public void visitJSFunctionDeclaration(JSFunction node) {
      resolveType(node.getReturnType());
    }

    @Override
    public void visitJSFunctionExpression(JSFunctionExpression node) {
      resolveType(node.getReturnType());
    }

    private void resolveType(@Nullable JSType type) {
      Set<JSResolvedTypeId> visitedTypes = new HashSet<>();
      while (type != null && visitedTypes.add(type.getResolvedTypeId())) {
        JSRecordType recordType = type.asRecordType();
        JSRecordType.PropertySignature ngModuleSignature;
        if (mySymbolClazz.isAssignableFrom(Angular2Module.class)
            && (ngModuleSignature = recordType.findPropertySignature("ngModule")) != null) {
          type = ngModuleSignature.getType();
        }
        else {
          PsiElement sourceElement = type.getSourceElement();
          if (sourceElement instanceof TypeScriptSingleType) {
            JSReferenceExpression expression = ((TypeScriptSingleType)sourceElement).getReferenceExpression();
            if (expression != null
                && tryAddEntity(ObjectUtils.tryCast(expression.resolve(), JSClass.class))) {
              return;
            }
          }
          JSRecordType.CallSignature constructor = ContainerUtil.find(recordType.getCallSignatures(),
                                                                      JSRecordType.CallSignature::hasNew);
          type = ObjectUtils.doIfNotNull(constructor, JSRecordType.CallSignature::getReturnType);
        }
      }
      myIsFullyResolved = false;
    }

    private boolean tryAddEntity(@Nullable JSClass aClass) {
      T entity = ObjectUtils.tryCast(Angular2EntitiesProvider.getEntity(aClass), mySymbolClazz);
      if (entity != null) {
        myResult.add(entity);
        return true;
      }
      return false;
    }

    private void push(@Nullable PsiElement element) {
      if (element != null) {
        myResolveStack.push(element);
      }
      else {
        myIsFullyResolved = false;
      }
    }
  }

  private static class ResolvedModule {
    final List<Angular2Entity> myExports;
    final List<Angular2Declaration> myDeclarations;
    final List<Angular2Module> myImports;
    final boolean myAreExportsFullyResolved;
    final boolean myIsScopeFullyResolved;
    final List<Object> myDependencies;

    private ResolvedModule(List<Angular2Entity> exports,
                           List<Angular2Declaration> declarations,
                           List<Angular2Module> imports,
                           boolean areExportsFullyResolved,
                           boolean isScopeFullyResolved,
                           List<Object> dependencies) {
      myExports = Collections.unmodifiableList(exports);
      myDeclarations = Collections.unmodifiableList(declarations);
      myImports = Collections.unmodifiableList(imports);
      myAreExportsFullyResolved = areExportsFullyResolved;
      myIsScopeFullyResolved = isScopeFullyResolved;
      myDependencies = Collections.unmodifiableList(dependencies);
    }
  }
}
