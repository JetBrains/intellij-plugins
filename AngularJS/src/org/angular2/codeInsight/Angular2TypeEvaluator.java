// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.ecmascript6.TypeScriptTypeEvaluator;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeUtils;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSGenericTypesEvaluatorBase;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.types.JSCompositeTypeImpl;
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl;
import com.intellij.lang.javascript.psi.types.JSTypeComparingContextService;
import com.intellij.lang.javascript.psi.types.JSTypeSubstitutor;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.MultiMap;
import one.util.streamex.StreamEx;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.intellij.lang.javascript.psi.types.JSUnionOrIntersectionType.OptimizedKind.OPTIMIZED_SIMPLE;

public class Angular2TypeEvaluator extends TypeScriptTypeEvaluator {

  public Angular2TypeEvaluator(JSEvaluateContext context,
                               JSTypeProcessor processor) {
    super(context, processor);
  }

  @Override
  protected boolean addTypeFromDialectSpecificElements(PsiElement resolveResult) {
    if (resolveResult instanceof Angular2TemplateBindings) {
      addTypeFromAngular2TemplateBindings((Angular2TemplateBindings)resolveResult);
      return true;
    }
    return super.addTypeFromDialectSpecificElements(resolveResult);
  }

  private void addTypeFromAngular2TemplateBindings(@NotNull Angular2TemplateBindings bindings) {
    JSImplicitElement templateDirective = DirectiveUtil.getAttributeDirective(
      "*" + bindings.getTemplateName(), bindings.getProject());
    if (templateDirective == null) {
      return;
    }
    Angular2Directive metadata = Angular2EntitiesProvider.getDirective(templateDirective);
    if (metadata.getTypeScriptClass() == null) {
      return;
    }
    JSType templateRefType = null;
    for (TypeScriptFunction fun : metadata.getTypeScriptClass().getConstructors()) {
      for (JSParameter param : fun.getParameterVariables()) {
        if (param.getType() != null && param.getType().getTypeText().startsWith("TemplateRef<")) {
          templateRefType = param.getType();
          break;
        }
      }
    }
    if (!(templateRefType instanceof JSGenericTypeImpl)) {
      return;
    }
    JSGenericTypeImpl templateRefGeneric = (JSGenericTypeImpl)templateRefType;
    if (templateRefGeneric.getArguments().isEmpty()) {
      return;
    }
    JSType templateContextType = templateRefGeneric.getArguments().get(0);
    addType(templateContextType instanceof JSGenericTypeImpl
            ? resolveTemplateContextTypeGeneric(metadata, (JSGenericTypeImpl)templateContextType, bindings)
            : templateContextType, bindings);
  }

  private static JSType resolveTemplateContextTypeGeneric(Angular2Directive metadata,
                                                          @NotNull JSGenericTypeImpl templateContextType,
                                                          @NotNull Angular2TemplateBindings bindings) {
    Map<String, Angular2TemplateBinding> bindingsMap = Arrays.stream(bindings.getBindings())
      .filter(b -> !b.keyIsVar())
      .collect(Collectors.toMap(Angular2TemplateBinding::getKey, Function.identity(), (a, b) -> a));

    MultiMap<JSTypeSubstitutor.JSTypeGenericId, JSType> genericArguments = MultiMap.createSmart();
    final ProcessingContext processingContext = JSTypeComparingContextService.getProcessingContextWithCache(metadata.getTypeScriptClass());

    metadata.getInputs().forEach(property -> {
      Angular2TemplateBinding binding = bindingsMap.get(property.getName());
      if (binding != null && property.getType() != null) {
        JSType expressionType = JSResolveUtil.getExpressionJSType(binding.getExpression());
        if (expressionType != null) {
          JSGenericTypesEvaluatorBase.matchGenericTypes(genericArguments, processingContext,
                                                        expressionType, property.getType(), null);
        }
      }
    });
    JSTypeSubstitutor substitutor = intersectGenerics(genericArguments, templateContextType);
    return JSTypeUtils.applyGenericArguments(templateContextType, substitutor);
  }

  private static JSTypeSubstitutor intersectGenerics(MultiMap<JSTypeSubstitutor.JSTypeGenericId, JSType> arguments,
                                                     JSGenericTypeImpl templateContextType) {
    JSTypeSubstitutor result = new JSTypeSubstitutor();
    for (Map.Entry<JSTypeSubstitutor.JSTypeGenericId, Collection<JSType>> entry : arguments.entrySet()) {
      List<JSType> types = StreamEx.of(entry.getValue()).nonNull().toList();
      if (types.size() == 1) {
        result.put(entry.getKey(), types.get(0));
        continue;
      }
      JSType type = new JSCompositeTypeImpl(templateContextType.getSource(), types);
      result.put(entry.getKey(), JSCompositeTypeImpl.optimizeTypeIfComposite(type, OPTIMIZED_SIMPLE));
    }
    return result;
  }
}
