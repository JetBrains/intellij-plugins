// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSGenericTypesEvaluator;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.types.JSCompositeTypeImpl;
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl;
import com.intellij.lang.javascript.psi.types.JSTypeComparingContextService;
import com.intellij.lang.javascript.psi.types.JSTypeSubstitutor;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.MultiMap;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.metadata.AngularDirectiveMetadata;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.psi.Angular2HtmlTemplateBindings;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Angular2TemplateBindingsContextResolver {

  @Nullable
  public static JSType getVariableType(JSVariable variable) {
    Angular2HtmlTemplateBindings bindings = PsiTreeUtil.getParentOfType(variable, Angular2HtmlTemplateBindings.class);
    Angular2TemplateBinding binding = PsiTreeUtil.getParentOfType(variable, Angular2TemplateBinding.class);
    if (binding == null || binding.getName() == null
        || bindings == null || bindings.getBindings() == null) {
      return null;
    }

    Ref<JSRecordType> res = new Ref<>();
    JSTypeEvaluator.processWithEvaluationGuard(
      variable, JSEvaluateContext.JSEvaluationPlace.TYPE_GUARD, v ->
        res.set(resolveTemplateContext(bindings.getTemplateName(), bindings.getBindings()))
    );
    if (res.isNull()) {
      return null;
    }
    JSRecordType.PropertySignature signature = res.get().findPropertySignature(binding.getName());
    return signature != null ? signature.getType() : null;
  }


  @Nullable
  private static JSRecordType resolveTemplateContext(@NotNull String templateName, @NotNull Angular2TemplateBindings bindings) {
    JSImplicitElement templateDirective = DirectiveUtil.getAttributeDirective(
      "*" + templateName, bindings.getProject());
    if (templateDirective == null) {
      return null;
    }
    AngularDirectiveMetadata metadata = AngularDirectiveMetadata.create(templateDirective);
    if (!(metadata.getDirectiveClass() instanceof TypeScriptClass)) {
      return null;
    }
    TypeScriptClass clazz = (TypeScriptClass)metadata.getDirectiveClass();
    JSType templateRefType = null;
    for (TypeScriptFunction fun : clazz.getConstructors()) {
      for (JSParameter param : fun.getParameterVariables()) {
        if (param.getType() != null && param.getType().getTypeText().startsWith("TemplateRef<")) {
          templateRefType = param.getType();
          break;
        }
      }
    }
    if (!(templateRefType instanceof JSGenericTypeImpl)) {
      return null;
    }
    JSGenericTypeImpl templateRefGeneric = (JSGenericTypeImpl)templateRefType;
    if (templateRefGeneric.getArguments().isEmpty()) {
      return null;
    }
    JSType templateContextType = templateRefGeneric.getArguments().get(0);
    return templateContextType instanceof JSGenericTypeImpl
           ? resolveTemplateContextTypeGeneric(metadata, (JSGenericTypeImpl)templateContextType, bindings)
           : templateContextType.asRecordType();
  }

  private static JSRecordType resolveTemplateContextTypeGeneric(AngularDirectiveMetadata metadata,
                                                                @NotNull JSGenericTypeImpl templateContextType,
                                                                @NotNull Angular2TemplateBindings bindings) {
    Map<String, Angular2TemplateBinding> bindingsMap = Arrays.stream(bindings.getBindings())
      .filter(b -> !b.keyIsVar())
      .collect(Collectors.toMap(Angular2TemplateBinding::getKey, Function.identity()));

    MultiMap<JSTypeSubstitutor.JSTypeGenericId, JSType> genericArguments = MultiMap.createSmart();
    final ProcessingContext processingContext = JSTypeComparingContextService.getProcessingContextWithCache(metadata.getDirectiveClass());

    metadata.getInputs().forEach(info -> {
      Angular2TemplateBinding binding = bindingsMap.get(info.name);
      if (binding != null && info.signature != null) {
        JSType expressionType = JSResolveUtil.getExpressionJSType(binding.getExpression());
        JSType paramType = info.signature.getType();
        if (expressionType != null && paramType != null) {
          JSGenericTypesEvaluator.matchGenericTypes(genericArguments, processingContext, expressionType, paramType, null);
        }
      }
    });
    JSTypeSubstitutor substitutor = intersectGenerics(genericArguments, templateContextType);
    JSType resultType = JSTypeUtils.applyGenericArguments(templateContextType, substitutor, false);
    return resultType.asRecordType();
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
      result.put(entry.getKey(), JSCompositeTypeImpl.optimizeTypeIfComposite(type, true));
    }
    return result;
  }
}
