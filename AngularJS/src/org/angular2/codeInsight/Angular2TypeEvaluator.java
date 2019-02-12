// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.ecmascript6.TypeScriptTypeEvaluator;
import com.intellij.lang.javascript.psi.JSCallItem;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeUtils;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSGenericTypesEvaluatorBase;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor;
import com.intellij.lang.javascript.psi.types.*;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.lang.javascript.psi.types.JSUnionOrIntersectionType.OptimizedKind.OPTIMIZED_SIMPLE;
import static com.intellij.util.containers.ContainerUtil.find;
import static com.intellij.util.containers.ContainerUtil.findAll;

public class Angular2TypeEvaluator extends TypeScriptTypeEvaluator {

  public Angular2TypeEvaluator(JSEvaluateContext context,
                               JSTypeProcessor processor) {
    super(context, processor);
  }

  public static JSType resolveType(@NotNull Angular2TemplateBindings bindings,
                                   @NotNull String key) {
    return BindingsTypeResolver.get(bindings).resolveDirectiveInputType(key);
  }

  @Override
  protected boolean addTypeFromDialectSpecificElements(PsiElement resolveResult) {
    if (resolveResult instanceof Angular2TemplateBindings) {
      JSType type = BindingsTypeResolver.get((Angular2TemplateBindings)resolveResult).resolveTemplateContextType();
      if (type != null) {
        addType(type, resolveResult);
      }
      return true;
    }
    return super.addTypeFromDialectSpecificElements(resolveResult);
  }

  @Contract("null -> null")
  private static JSType getTemplateContextType(@Nullable TypeScriptClass clazz) {
    if (clazz == null) {
      return null;
    }
    JSType templateRefType = null;
    for (TypeScriptFunction fun : clazz.getConstructors()) {
      for (JSParameter param : fun.getParameterVariables()) {
        if (param.getJSType() != null && param.getJSType().getTypeText().startsWith("TemplateRef<")) {
          templateRefType = param.getJSType();
          break;
        }
      }
    }
    if (!(templateRefType instanceof JSGenericTypeImpl)) {
      return null;
    }
    return ContainerUtil.getFirstItem(((JSGenericTypeImpl)templateRefType).getArguments());
  }

  private static JSTypeSubstitutor intersectGenerics(MultiMap<JSTypeSubstitutor.JSTypeGenericId, JSType> arguments,
                                                     JSTypeSource source) {
    JSTypeSubstitutor result = new JSTypeSubstitutor();
    for (Map.Entry<JSTypeSubstitutor.JSTypeGenericId, Collection<JSType>> entry : arguments.entrySet()) {
      result.put(entry.getKey(), merge(source, ContainerUtil.filter(entry.getValue(), Objects::nonNull), false));
    }
    return result;
  }

  @NotNull
  private static JSType merge(JSTypeSource source, List<JSType> types, boolean union) {
    if (types.size() == 1) {
      return types.get(0);
    }
    return JSCompositeTypeImpl.optimizeTypeIfComposite(
      union ? new JSCompositeTypeImpl(source, types)
            : new JSIntersectionTypeImpl(source, types), OPTIMIZED_SIMPLE);
  }

  private static class BindingsTypeResolver {

    private final List<Angular2Directive> myMatched;
    private final Angular2DeclarationsScope myScope;
    private final JSType myRawTemplateContextType;
    private final JSTypeSubstitutor myTypeSubstitutor;

    private BindingsTypeResolver(@NotNull Angular2TemplateBindings bindings) {
      myMatched = new Angular2ApplicableDirectivesProvider(bindings).getMatched();
      myScope = new Angular2DeclarationsScope(bindings);

      List<Angular2Directive> templateDirectives = findAll(
        myMatched, directive -> myScope.contains(directive) && directive.isTemplate());

      if (templateDirectives.isEmpty()) {
        myRawTemplateContextType = null;
        myTypeSubstitutor = null;
        return;
      }
      Pair<JSType, JSTypeSubstitutor> analyzed = analyze(templateDirectives, bindings);
      myRawTemplateContextType = analyzed.first;
      myTypeSubstitutor = analyzed.second;
    }

    @NotNull
    public static BindingsTypeResolver get(Angular2TemplateBindings bindings) {
      return CachedValuesManager.getCachedValue(bindings, () -> CachedValueProvider.Result.create(
        new BindingsTypeResolver(bindings), PsiModificationTracker.MODIFICATION_COUNT));
    }

    public JSType resolveDirectiveInputType(String key) {
      for (Angular2Directive directive : myMatched) {
        Angular2DirectiveProperty property;
        if (myScope.contains(directive)
            && (property = find(directive.getInputs(), input -> input.getName().equals(key))) != null) {
          JSType type = property.getType();
          if (myTypeSubstitutor != null) {
            return JSTypeUtils.applyGenericArguments(type, myTypeSubstitutor);
          }
          return type;
        }
      }
      return null;
    }

    @Nullable
    public JSType resolveTemplateContextType() {
      return myTypeSubstitutor != null
             ? JSTypeUtils.applyGenericArguments(myRawTemplateContextType, myTypeSubstitutor)
             : myRawTemplateContextType;
    }

    private static Pair<JSType, JSTypeSubstitutor> analyze(@NotNull List<Angular2Directive> directives,
                                                           @NotNull Angular2TemplateBindings bindings) {
      Map<String, Angular2TemplateBinding> bindingsMap = new HashMap<>();
      for (Angular2TemplateBinding templateBinding : bindings.getBindings()) {
        if (!templateBinding.keyIsVar()) {
          bindingsMap.putIfAbsent(templateBinding.getKey(), templateBinding);
        }
      }

      MultiMap<JSTypeSubstitutor.JSTypeGenericId, JSType> genericArguments = MultiMap.createSmart();
      List<JSType> contextTypes = new SmartList<>();
      directives.forEach(directive -> {
        JSType contextType = getTemplateContextType(directive.getTypeScriptClass());
        if (contextType == null) {
          return;
        }
        contextTypes.add(contextType);
        if (contextType instanceof JSGenericTypeImpl) {
          final ProcessingContext processingContext =
            JSTypeComparingContextService.getProcessingContextWithCache(directive.getTypeScriptClass());
          directive.getInputs().forEach(property -> {
            Angular2TemplateBinding binding = bindingsMap.get(property.getName());
            if (binding != null && property.getType() != null) {
              JSType expressionType = JSResolveUtil.getExpressionJSType(binding.getExpression());
              if (expressionType != null) {
                JSGenericTypesEvaluatorBase.matchGenericTypes(genericArguments, processingContext,
                                                              expressionType, property.getType(), null, (JSCallItem)null);
              }
            }
          });
        }
      });
      if (contextTypes.isEmpty()) {
        return Pair.pair(null, null);
      }
      JSType templateContextType = merge(contextTypes.get(0).getSource(), contextTypes, true);
      if (genericArguments.isEmpty()) {
        return Pair.pair(templateContextType, null);
      }
      return Pair.pair(templateContextType, intersectGenerics(genericArguments, templateContextType.getSource()));
    }
  }
}
