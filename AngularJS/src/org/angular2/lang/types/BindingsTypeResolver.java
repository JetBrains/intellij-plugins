// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeWithIncompleteSubstitution;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.resolve.JSGenericMappings;
import com.intellij.lang.javascript.psi.resolve.JSGenericTypesEvaluatorBase;
import com.intellij.lang.javascript.psi.resolve.generic.JSTypeSubstitutorImpl;
import com.intellij.lang.javascript.psi.types.*;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.intellij.util.containers.MultiMap;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.Angular2LibrariesHacks;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.entities.Angular2ComponentLocator;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.intellij.lang.javascript.psi.JSTypeUtils.*;
import static com.intellij.lang.javascript.psi.types.JSUnionOrIntersectionType.OptimizedKind.OPTIMIZED_SIMPLE;
import static com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations.expandAndOptimizeTypeRecursive;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.containers.ContainerUtil.*;
import static org.angular2.entities.Angular2EntityUtils.TEMPLATE_REF;
import static org.angular2.lang.types.Angular2TypeUtils.extractEventVariableType;

final class BindingsTypeResolver {

  private final @NotNull PsiElement myElement;
  private final @NotNull List<Angular2Directive> myMatched;
  private final @NotNull Angular2DeclarationsScope myScope;
  private final @Nullable JSType myRawTemplateContextType;
  private final @Nullable JSTypeSubstitutor myTypeSubstitutor;


  @SuppressWarnings("BoundedWildcard")
  public static @Nullable JSType resolve(@NotNull XmlAttribute attribute,
                                         @NotNull Predicate<Angular2AttributeNameParser.AttributeInfo> infoValidation,
                                         @NotNull BiFunction<BindingsTypeResolver, String, JSType> resolveMethod) {
    Angular2AttributeDescriptor descriptor = ObjectUtils.tryCast(attribute.getDescriptor(), Angular2AttributeDescriptor.class);
    XmlTag tag = attribute.getParent();
    var info = Angular2AttributeNameParser.parse(attribute.getName(), attribute.getParent());
    if (descriptor == null || tag == null || !infoValidation.test(info)) {
      return null;
    }
    return resolveMethod.apply(get(tag), info.name);
  }

  public @Nullable JSType resolveDirectiveEventType(@NotNull String name) {
    List<JSType> types = new SmartList<>();
    for (Angular2Directive directive : myMatched) {
      Angular2DirectiveProperty property;
      if (myScope.contains(directive)
          && (property = find(directive.getOutputs(), output -> output.getName().equals(name))) != null) {
        types.add(Angular2LibrariesHacks.hackNgModelChangeType(property.getJsType(), name));
      }
    }
    return processAndMerge(types);
  }

  public @Nullable JSType resolveDirectiveInputType(@NotNull String key) {
    List<JSType> types = new SmartList<>();
    for (Angular2Directive directive : myMatched) {
      Angular2DirectiveProperty property;
      if (myScope.contains(directive)
          && (property = find(directive.getInputs(), input -> input.getName().equals(key))) != null) {
        types.add(property.getJsType());
      }
    }
    return processAndMerge(types);
  }

  public @Nullable JSType resolveTemplateContextType() {
    return myTypeSubstitutor != null
           ? applyGenericArguments(myRawTemplateContextType, myTypeSubstitutor)
           : myRawTemplateContextType;
  }

  public static @NotNull BindingsTypeResolver get(@NotNull XmlTag tag) {
    return CachedValuesManager.getCachedValue(tag, () -> CachedValueProvider.Result.create(
      new BindingsTypeResolver(tag), PsiModificationTracker.MODIFICATION_COUNT));
  }

  public static @NotNull BindingsTypeResolver get(@NotNull Angular2TemplateBindings bindings) {
    return CachedValuesManager.getCachedValue(bindings, () -> CachedValueProvider.Result.create(
      new BindingsTypeResolver(bindings), PsiModificationTracker.MODIFICATION_COUNT));
  }

  private BindingsTypeResolver(@NotNull Angular2TemplateBindings bindings) {
    this(bindings, new Angular2ApplicableDirectivesProvider(bindings), b ->
      StreamEx.of(b.getBindings())
        .filter(binding -> !binding.keyIsVar())
        .mapToEntry(Angular2TemplateBinding::getKey,
                    Angular2TemplateBinding::getExpression)
    );
  }

  private BindingsTypeResolver(@NotNull XmlTag tag) {
    this(tag, new Angular2ApplicableDirectivesProvider(tag), t ->
      StreamEx.of(t.getAttributes())
        .mapToEntry(attr -> Angular2AttributeNameParser.parse(attr.getName(), attr.getParent()),
                    Function.identity())
        .filterKeys(Angular2PropertyBindingType::isPropertyBindingAttribute)
        .mapValues(attribute -> doIfNotNull(Angular2Binding.get(attribute),
                                            Angular2Binding::getExpression))
        .mapKeys(info -> info.name));
  }

  private <T extends PsiElement> BindingsTypeResolver(@NotNull T element,
                                                      @NotNull Angular2ApplicableDirectivesProvider provider,
                                                      @NotNull Function<T, EntryStream<String, JSExpression>> inputExpressionsProvider) {
    myElement = element;
    myMatched = provider.getMatched();
    myScope = new Angular2DeclarationsScope(element);

    List<Angular2Directive> directives = findAll(
      myMatched, directive -> myScope.contains(directive));

    if (directives.isEmpty()) {
      myRawTemplateContextType = null;
      myTypeSubstitutor = null;
      return;
    }
    Pair<JSType, JSTypeSubstitutor> analyzed = analyze(directives, element, inputExpressionsProvider);
    myRawTemplateContextType = analyzed.first;
    myTypeSubstitutor = analyzed.second;
  }

  private @Nullable JSType processAndMerge(@NotNull List<JSType> types) {
    types = filter(types, Objects::nonNull);
    JSTypeSource source = getTypeSource(myElement, types);
    if (source == null || types.isEmpty()) {
      return null;
    }
    if (myTypeSubstitutor != null) {
      types = map(types, type -> applyGenericArguments(type, myTypeSubstitutor));
    }
    return merge(source, types, false);
  }

  private static @NotNull <T extends PsiElement> Pair<JSType, JSTypeSubstitutor> analyze(@NotNull List<Angular2Directive> directives,
                                                                                         @NotNull T element,
                                                                                         @NotNull Function<T, EntryStream<String, JSExpression>> inputExpressionsProvider) {
    Map<String, JSExpression> inputsMap = inputExpressionsProvider
      .apply(element)
      .nonNullValues()
      .toMap((a, b) -> a);
    MultiMap<JSTypeSubstitutor.JSTypeGenericId, JSType> genericArguments = new MultiMap<>();
    List<JSType> templateContextTypes = new SmartList<>();
    directives.forEach(directive -> {
      TypeScriptClass clazz = directive.getTypeScriptClass();
      if (clazz == null) {
        return;
      }
      JSType templateContextType = getTemplateContextType(clazz);
      if (templateContextType != null) {
        templateContextTypes.add(templateContextType);
      }
      final ProcessingContext processingContext =
        JSTypeComparingContextService.createProcessingContextWithCache(clazz);
      directive.getInputs().forEach(property -> {
        JSExpression inputExpression = inputsMap.get(property.getName());
        JSType propertyType;
        if (inputExpression != null && (propertyType = property.getJsType()) != null) {
          JSPsiBasedTypeOfType inputType = new JSPsiBasedTypeOfType(inputExpression, true);
          if (isAnyType(getApparentType(JSTypeWithIncompleteSubstitution.substituteCompletely(inputType)))) {
            // This workaround is needed, because many users expect to have ngForOf working with variable of type `any`.
            // This is not correct according to TypeScript inferring rules for generics, but it's better for Angular type
            // checking to be less strict here. Additionally, if `any` type is passed to e.g. async pipe it's going to be resolved
            // with `null`, so we need to check for `null` and `undefined` as well
            JSAnyType anyType = JSAnyType.get(inputType.getSource());
            expandAndOptimizeTypeRecursive(propertyType).accept(new JSRecursiveTypeVisitor(true) {
              @Override
              public void visitJSType(@NotNull JSType type) {
                if (type instanceof JSGenericParameterType) {
                  genericArguments.putValue(((JSGenericParameterType)type).getGenericId(), anyType);
                }
                super.visitJSType(type);
              }
            });
          }
          else {
            JSGenericTypesEvaluatorBase
              .matchGenericTypes(new JSGenericMappings(genericArguments), processingContext, inputType, propertyType, x -> true);
            JSGenericTypesEvaluatorBase
              .widenInferredTypes(genericArguments, Collections.singletonList(propertyType), null, null, processingContext);
          }
        }
      });
    });
    JSTypeSource typeSource = getTypeSource(element, templateContextTypes, genericArguments);
    return Pair.pair(
      typeSource == null || templateContextTypes.isEmpty() ? null : merge(typeSource, templateContextTypes, true),
      typeSource == null || genericArguments.isEmpty() ? null : intersectGenerics(genericArguments, typeSource));
  }

  private static @NotNull JSTypeSubstitutor intersectGenerics(@NotNull MultiMap<JSTypeSubstitutor.JSTypeGenericId, JSType> arguments,
                                                              @NotNull JSTypeSource source) {
    JSTypeSubstitutorImpl result = new JSTypeSubstitutorImpl();
    for (Map.Entry<JSTypeSubstitutor.JSTypeGenericId, Collection<JSType>> entry : arguments.entrySet()) {
      result.put(entry.getKey(), merge(source, filter(entry.getValue(), Objects::nonNull), false));
    }
    return result;
  }

  private static @NotNull JSType merge(@NotNull JSTypeSource source, @NotNull List<JSType> types, boolean union) {
    return JSCompositeTypeImpl.optimizeTypeIfComposite(
      union ? JSCompositeTypeFactory.createUnionType(source, types)
            : JSCompositeTypeFactory.createIntersectionType(types, source), OPTIMIZED_SIMPLE);
  }


  private static @Nullable JSTypeSource getTypeSource(@NotNull PsiElement element,
                                                      @NotNull List<JSType> templateContextTypes,
                                                      @NotNull MultiMap<JSTypeSubstitutor.JSTypeGenericId, JSType> genericArguments) {
    JSTypeSource source = getTypeSource(element, templateContextTypes);
    return source != null ? source : doIfNotNull(find(genericArguments.values(), Objects::nonNull), JSType::getSource);
  }

  private static @Nullable JSTypeSource getTypeSource(@NotNull PsiElement element,
                                                      @NotNull List<JSType> types) {
    TypeScriptClass componentClass = Angular2ComponentLocator.findComponentClass(element);
    if (componentClass != null) {
      return JSTypeSourceFactory.createTypeSource(componentClass, true);
    }
    return doIfNotNull(getFirstItem(types), JSType::getSource);
  }

  @Contract("null -> null") //NON-NLS
  private static JSType getTemplateContextType(@Nullable TypeScriptClass clazz) {
    if (clazz == null) {
      return null;
    }
    JSType templateRefType = null;
    for (TypeScriptFunction fun : clazz.getConstructors()) {
      for (JSParameter param : fun.getParameterVariables()) {
        if (param.getJSType() != null && param.getJSType().getTypeText().startsWith(TEMPLATE_REF + "<")) {
          templateRefType = param.getJSType();
          break;
        }
      }
    }
    if (!(templateRefType instanceof JSGenericTypeImpl)) {
      return null;
    }
    return getFirstItem(((JSGenericTypeImpl)templateRefType).getArguments());
  }
}