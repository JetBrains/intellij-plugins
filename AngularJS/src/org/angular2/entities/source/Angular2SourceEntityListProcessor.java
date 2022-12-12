// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.ecmascript6.psi.ES6ImportExportSpecifierAlias;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType;
import com.intellij.lang.javascript.psi.ecma6.impl.TypeScriptFunctionCachingVisitor;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSFunctionBaseImpl;
import com.intellij.lang.javascript.psi.impl.JSFunctionCachedDataBuilder;
import com.intellij.lang.javascript.psi.impl.JSFunctionNodesVisitor;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl;
import com.intellij.lang.javascript.psi.types.JSTypeImpl;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.AstLoadingFilter;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2Module;
import org.angular2.entities.metadata.Angular2MetadataUtil;
import org.angular2.entities.metadata.psi.Angular2MetadataFunction;
import org.angular2.entities.metadata.psi.Angular2MetadataModule;
import org.angular2.entities.metadata.psi.Angular2MetadataObject;
import org.angular2.entities.metadata.psi.Angular2MetadataReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static com.intellij.util.containers.ContainerUtil.addIfNotNull;
import static java.util.Arrays.asList;
import static org.angular2.entities.Angular2ModuleResolver.MODULE_WITH_PROVIDERS_CLASS;
import static org.angular2.entities.Angular2ModuleResolver.NG_MODULE_PROP;
import static org.angular2.entities.ivy.Angular2IvyUtil.getIvyEntity;

public abstract class Angular2SourceEntityListProcessor<T extends Angular2Entity> {

  private final Class<T> myAcceptableEntityClass;
  private final boolean myAcceptNgModuleWithProviders;
  private final JSElementVisitor myResultsVisitor = new JSElementVisitor() {
    @Override
    public void visitJSClass(JSClass aClass) {
      T entity = getAcceptableEntity(aClass);
      if (entity != null) {
        processAcceptableEntity(entity);
      }
      else {
        processNonAcceptableEntityClass(aClass);
      }
    }

    @Override
    public void visitJSArrayLiteralExpression(JSArrayLiteralExpression node) {
      //it's ok, if array does not have any children
    }

    @Override
    public void visitJSFunctionDeclaration(JSFunction node) {
      resolveFunctionReturnType(node);
    }

    @Override
    public void visitJSFunctionExpression(JSFunctionExpression node) {
      resolveFunctionReturnType(node);
    }

    @Override
    public void visitJSElement(JSElement node) {
      processAnyElement(node);
    }
  };

  public Angular2SourceEntityListProcessor(@NotNull Class<T> acceptableEntityClass) {
    myAcceptableEntityClass = acceptableEntityClass;
    myAcceptNgModuleWithProviders = myAcceptableEntityClass.isAssignableFrom(Angular2Module.class);
  }

  protected final List<PsiElement> resolve(PsiElement t) {
    SmartList<PsiElement> result = new SmartList<>();
    t.accept(createResolveVisitor(result));
    return result;
  }

  private JSElementVisitor createResolveVisitor(SmartList<PsiElement> result) {
    return new JSElementVisitor() {
      @Override
      public void visitJSArrayLiteralExpression(JSArrayLiteralExpression node) {
        result.addAll(asList(node.getExpressions()));
      }

      @Override
      public void visitJSObjectLiteralExpression(JSObjectLiteralExpression node) {
        if (myAcceptNgModuleWithProviders) {
          AstLoadingFilter.forceAllowTreeLoading(node.getContainingFile(), () ->
            addIfNotNull(result, doIfNotNull(node.findProperty(NG_MODULE_PROP), JSProperty::getValue)));
        }
      }

      @Override
      public void visitJSReferenceExpression(JSReferenceExpression node) {
        addIfNotNull(result, node.resolve());
      }

      @Override
      public void visitJSVariable(JSVariable node) {
        AstLoadingFilter.forceAllowTreeLoading(node.getContainingFile(), () ->
          addIfNotNull(result, node.getInitializer()));
      }

      @Override
      public void visitJSProperty(JSProperty node) {
        AstLoadingFilter.forceAllowTreeLoading(node.getContainingFile(), () ->
          addIfNotNull(result, node.getValue()));
      }

      @Override
      public void visitES6ImportExportSpecifierAlias(ES6ImportExportSpecifierAlias alias) {
        addIfNotNull(result, alias.findAliasedElement());
      }

      @Override
      public void visitJSSpreadExpression(JSSpreadExpression spreadExpression) {
        AstLoadingFilter.forceAllowTreeLoading(spreadExpression.getContainingFile(), () ->
          addIfNotNull(result, spreadExpression.getExpression()));
      }

      @Override
      public void visitJSConditionalExpression(JSConditionalExpression node) {
        AstLoadingFilter.forceAllowTreeLoading(node.getContainingFile(), () -> {
          addIfNotNull(result, node.getThenBranch());
          addIfNotNull(result, node.getElseBranch());
        });
      }

      @Override
      public void visitJSCallExpression(JSCallExpression node) {
        addIfNotNull(result, node.getStubSafeMethodExpression());
      }

      @Override
      public void visitJSFunctionDeclaration(JSFunction node) {
        collectFunctionReturningArrayItems(node);
      }

      @Override
      public void visitJSFunctionExpression(JSFunctionExpression node) {
        collectFunctionReturningArrayItems(node);
      }

      private void collectFunctionReturningArrayItems(@NotNull JSFunction function) {
        JSType type = function.getReturnType();
        if (JSTypeUtils.isArrayLikeType(type)) {
          // None of the required information is stubbed here
          AstLoadingFilter.forceAllowTreeLoading(function.getContainingFile(), () ->
            function.acceptChildren(new JSElementVisitor() {
              @Override
              public void visitJSReturnStatement(JSReturnStatement node) {
                addIfNotNull(result, node.getExpression());
              }

              @Override
              public void visitJSStatement(JSStatement node) {
                node.acceptChildren(this);
              }
            })
          );
        }
      }
    };
  }

  protected final JSElementVisitor getResultsVisitor() {
    return myResultsVisitor;
  }

  /**
   * @see #getAcceptableEntity
   */
  protected void processNonAcceptableEntityClass(@NotNull JSClass aClass) {

  }

  /**
   * @see #getAcceptableEntity
   */
  protected void processAcceptableEntity(@NotNull T entity) {

  }

  protected void processAnyType() {

  }

  protected void processAnyElement(JSElement node) {

  }

  /**
   * Implementations can store the {@code element} and pass it later to {@link CachedValueProvider.Result#create(Object, Collection)}
   */
  protected void processCacheDependency(PsiElement element) {

  }

  private T getAcceptableEntity(@Nullable JSClass aClass) {
    return tryCast(Angular2EntitiesProvider.getEntity(aClass), myAcceptableEntityClass);
  }

  private void resolveFunctionReturnType(@NotNull JSFunction function) {
    Set<JSResolvedTypeId> visitedTypes = new HashSet<>();
    boolean lookingForModule = myAcceptNgModuleWithProviders;
    JSClass resolvedClazz = null;
    JSType type = function.getReturnType();
    while (type != null
           && !(type instanceof JSAnyType)
           && visitedTypes.add(type.getResolvedTypeId())) {
      NotNullLazyValue<JSRecordType> recordType = NotNullLazyValue.createValue(type::asRecordType);
      JSRecordType.PropertySignature ngModuleSignature;
      if (type.getSourceElement() != null) {
        processCacheDependency(type.getSourceElement());
      }
      if (lookingForModule) { // see https://angular.io/guide/migration-module-with-providers
        // Ivy syntax
        if (type instanceof JSGenericTypeImpl
            && ((JSGenericTypeImpl)type).getType() instanceof JSTypeImpl
            && MODULE_WITH_PROVIDERS_CLASS.equals(((JSGenericTypeImpl)type).getType().getTypeText())) {
          JSType argument = ContainerUtil.getFirstItem(((JSGenericTypeImpl)type).getArguments());
          if (argument != null && !(argument instanceof JSAnyType)) {
            type = argument;
            lookingForModule = false;
            continue;
          }
        }
        // pre-Ivy syntax
        if ((ngModuleSignature = recordType.getValue().findPropertySignature(NG_MODULE_PROP)) != null) {
          type = evaluateModuleWithProvidersType(ngModuleSignature, type.getSource());
          lookingForModule = false;
          continue;
        }
      }
      PsiElement sourceElement = type.getSourceElement();
      if (sourceElement instanceof TypeScriptSingleType) {
        JSReferenceExpression expression = AstLoadingFilter.forceAllowTreeLoading(
          sourceElement.getContainingFile(), ((TypeScriptSingleType)sourceElement)::getReferenceExpression);
        if (expression != null) {
          resolvedClazz = tryCast(expression.resolve(), JSClass.class);
          T entity = getAcceptableEntity(resolvedClazz);
          if (entity != null) {
            processCacheDependency(resolvedClazz);
            processAcceptableEntity(entity);
            return;
          }
        }
      }
      else if (sourceElement instanceof TypeScriptClass) {
        resolvedClazz = (JSClass)sourceElement;
        T entity = getAcceptableEntity(resolvedClazz);
        if (entity != null) {
          processCacheDependency(resolvedClazz);
          processAcceptableEntity(entity);
          return;
        }
      }
      JSRecordType.CallSignature constructor = ContainerUtil.find(recordType.getValue().getCallSignatures(),
                                                                  JSRecordType.CallSignature::hasNew);
      type = doIfNotNull(constructor, JSRecordType.CallSignature::getReturnType);
    }
    // Fallback to search in metadata
    if (myAcceptNgModuleWithProviders) {
      Angular2MetadataFunction metadataFunction = Angular2MetadataUtil.findMetadataFunction(function);
      Angular2MetadataModule metadataModule = resolveFunctionValue(metadataFunction);
      if (metadataModule != null) {
        processCacheDependency(metadataFunction);
        processCacheDependency(metadataModule);
        // Make sure we translate to Ivy module if available
        TypeScriptClass tsClass = metadataModule.getTypeScriptClass();
        if (tsClass != null) {
          Angular2Module ivyModule = tryCast(getIvyEntity(tsClass), Angular2Module.class);
          if (ivyModule != null) {
            processCacheDependency(tsClass);
            //noinspection unchecked
            processAcceptableEntity((T)ivyModule);
            return;
          }
        }
        //noinspection unchecked
        processAcceptableEntity((T)metadataModule);
        return;
      }
    }
    if (resolvedClazz != null && !(type instanceof JSAnyType)) {
      processNonAcceptableEntityClass(resolvedClazz);
    }
    else {
      processAnyType();
    }
  }

  private static JSType evaluateModuleWithProvidersType(JSRecordType.PropertySignature ngModuleSignature, JSTypeSource functionTypeSource) {
    JSType result = ngModuleSignature.getJSType();
    List<JSType> args;
    JSFunctionBaseImpl<?> function;

    if (result instanceof JSGenericTypeImpl
        && (args = ((JSGenericTypeImpl)result).getArguments()).size() == 1
        && args.get(0) instanceof JSAnyType
        && functionTypeSource.getSourceElement() != null
        && (function = tryCast(functionTypeSource.getSourceElement().getContext(), JSFunctionBaseImpl.class)) != null) {

      JSType evaluatedReturnType = CachedValuesManager.getCachedValue(function, () -> {
        final JSFunctionCachedDataBuilder cachedData = new JSFunctionCachedDataBuilder();
        final List<JSFunction> nestedFuns = new SmartList<>();
        final JSFunctionNodesVisitor cachedDataEvaluator =
          new TypeScriptFunctionCachingVisitor(function,
                                               cachedData, nestedFuns);
        AstLoadingFilter.forceAllowTreeLoading(
          function.getContainingFile(),
          () -> cachedDataEvaluator.visitElement(function.getNode()));
        return CachedValueProvider.Result.create(cachedDataEvaluator.getReturnTypeFromEvaluated(), function);
      });

      ngModuleSignature = doIfNotNull(evaluatedReturnType,
                                      t -> t.asRecordType().findPropertySignature(NG_MODULE_PROP));
      result = doIfNotNull(ngModuleSignature, JSRecordType.PropertySignature::getJSType);
    }
    return result;
  }

  private static @Nullable Angular2MetadataModule resolveFunctionValue(@Nullable Angular2MetadataFunction function) {
    return Optional.ofNullable(function)
      .map(f -> tryCast(f.getValue(), Angular2MetadataObject.class))
      .map(value -> tryCast(value.findMember(NG_MODULE_PROP), Angular2MetadataReference.class))
      .map(reference -> tryCast(reference.resolve(), Angular2MetadataModule.class))
      .orElse(null);
  }
}
