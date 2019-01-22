// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.Stack;
import org.angular2.entities.*;
import org.angular2.entities.metadata.psi.Angular2MetadataFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.angular2.Angular2DecoratorUtil.getProperty;
import static org.angular2.entities.Angular2ModuleResolver.NG_MODULE_PROP;

public class Angular2SourceModule extends Angular2SourceEntity implements Angular2Module {

  private final Angular2ModuleResolver<ES6Decorator> myModuleResolver = new Angular2ModuleResolver<>(
    this::getDecorator, Angular2SourceModule::collectSymbols);

  public Angular2SourceModule(@NotNull ES6Decorator decorator,
                              @NotNull JSImplicitElement implicitElement) {
    super(decorator, implicitElement);
  }

  @Override
  @NotNull
  public Set<Angular2Declaration> getDeclarations() {
    return myModuleResolver.getDeclarations();
  }

  @Override
  @NotNull
  public Set<Angular2Module> getImports() {
    return myModuleResolver.getImports();
  }

  @Override
  @NotNull
  public Set<Angular2Entity> getExports() {
    return myModuleResolver.getExports();
  }

  @Override
  public boolean isScopeFullyResolved() {
    return myModuleResolver.isScopeFullyResolved();
  }

  @Override
  public boolean areExportsFullyResolved() {
    return myModuleResolver.areExportsFullyResolved();
  }


  private static <T extends Angular2Entity> Pair<Set<T>, Boolean> collectSymbols(@NotNull ES6Decorator decorator,
                                                                                  @NotNull String propertyName,
                                                                                  @NotNull Class<T> symbolClazz) {
    JSProperty property = getProperty(decorator, propertyName);
    if (property == null) {
      return Pair.pair(Collections.emptySet(), true);
    }
    return new SourceSymbolCollector<>(symbolClazz).collect(property.getValue());
  }

  private static class SourceSymbolCollector<T extends Angular2Entity> extends JSElementVisitor {

    private final Stack<PsiElement> myResolveStack = new Stack<>();
    private final Class<T> mySymbolClazz;
    private final Set<T> myResult = new HashSet<>();
    private boolean myIsFullyResolved = true;

    SourceSymbolCollector(@NotNull Class<T> symbolClazz) {
      mySymbolClazz = symbolClazz;
    }

    public Pair<Set<T>, Boolean> collect(@Nullable JSExpression value) {
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
      resolveFunctionReturnType(node);
    }

    @Override
    public void visitJSFunctionExpression(JSFunctionExpression node) {
      resolveFunctionReturnType(node);
    }

    private void resolveFunctionReturnType(@NotNull JSFunction function) {
      Set<JSResolvedTypeId> visitedTypes = new HashSet<>();
      boolean lookingForModule = mySymbolClazz.isAssignableFrom(Angular2Module.class);
      if (lookingForModule) {
        Angular2MetadataFunction metadataFunction = Angular2EntitiesProvider.findMetadataFunction(function);
        Angular2Module module;
        if (metadataFunction != null
            && (module = metadataFunction.getReferencedModule()) != null) {
          //noinspection unchecked
          myResult.add((T)module);
          return;
        }
      }
      JSType type = function.getReturnType();
      while (type != null && visitedTypes.add(type.getResolvedTypeId())) {
        JSRecordType recordType = type.asRecordType();
        JSRecordType.PropertySignature ngModuleSignature;
        if (lookingForModule
            && (ngModuleSignature = recordType.findPropertySignature(NG_MODULE_PROP)) != null) {
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
}
