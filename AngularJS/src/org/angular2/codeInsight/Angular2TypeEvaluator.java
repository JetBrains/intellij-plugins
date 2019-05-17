// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.ecmascript6.TypeScriptTypeEvaluator;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSGenericMappings;
import com.intellij.lang.javascript.psi.resolve.JSGenericTypesEvaluatorBase;
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor;
import com.intellij.lang.javascript.psi.types.*;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.containers.Predicate;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.index.Angular2IndexingHandler;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.PropertyBindingInfo;
import org.angular2.lang.html.psi.Angular2HtmlEvent.EventType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.intellij.lang.javascript.psi.JSTypeUtils.*;
import static com.intellij.lang.javascript.psi.types.JSUnionOrIntersectionType.OptimizedKind.OPTIMIZED_SIMPLE;
import static com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeRelations.expandAndOptimizeTypeRecursive;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.containers.ContainerUtil.*;
import static org.angular2.entities.Angular2EntityUtils.TEMPLATE_REF;
import static org.angular2.lang.html.parser.Angular2AttributeType.*;
import static org.angular2.lang.html.psi.PropertyBindingType.PROPERTY;

public class Angular2TypeEvaluator extends TypeScriptTypeEvaluator {

  public Angular2TypeEvaluator(@NotNull JSEvaluateContext context,
                               @NotNull JSTypeProcessor processor) {
    super(context, processor, Angular2TypeEvaluationHelper.INSTANCE);
  }

  @Nullable
  public static JSType getEventVariableType(@Nullable JSType type) {
    if (type == null) {
      return null;
    }
    List<JSType> result = new ArrayList<>();
    processExpandedType(subType -> {
      if (subType instanceof JSGenericTypeImpl) {
        List<JSType> arguments = ((JSGenericTypeImpl)subType).getArguments();
        if (arguments.size() == 1) {
          result.add(arguments.get(0));
        }
        return false;
      }
      else if (subType instanceof JSFunctionType) {
        List<JSParameterTypeDecorator> params = ((JSFunctionType)subType).getParameters();
        if (params.size() == 1 && !params.get(0).isRest()) {
          result.add(params.get(0).getSimpleType());
        }
        return false;
      }
      return true;
    }, type);
    if (result.isEmpty()) {
      return null;
    }
    return JSCompositeTypeImpl.getCommonType(result, type.getSource(), false);
  }

  @Nullable
  public static JSType resolvePropertyType(@NotNull Angular2TemplateBindings bindings,
                                           @NotNull String key) {
    return BindingsTypeResolver.get(bindings).resolveDirectiveInputType(key);
  }

  @Nullable
  public static JSType resolvePropertyType(@NotNull XmlAttribute attribute) {
    return resolveType(attribute,
                       Angular2TypeEvaluator::isPropertyBindingAttribute,
                       BindingsTypeResolver::resolveDirectiveInputType);
  }

  @Nullable
  public static JSType resolveEventType(@NotNull XmlAttribute attribute) {
    Angular2AttributeDescriptor descriptor = ObjectUtils.tryCast(attribute.getDescriptor(), Angular2AttributeDescriptor.class);
    if (descriptor != null && isEmpty(descriptor.getSourceDirectives())) {
      return getEventVariableType(descriptor.getJSType());
    }
    return resolveType(attribute,
                       Angular2TypeEvaluator::isEventAttribute,
                       BindingsTypeResolver::resolveDirectiveEventType);
  }

  @Nullable
  private static JSType resolveType(@NotNull XmlAttribute attribute,
                                    @NotNull Predicate<AttributeInfo> infoValidation,
                                    @NotNull BiFunction<BindingsTypeResolver, String, JSType> resolveMethod) {
    Angular2AttributeDescriptor descriptor = ObjectUtils.tryCast(attribute.getDescriptor(), Angular2AttributeDescriptor.class);
    XmlTag tag = attribute.getParent();
    if (descriptor == null || tag == null || !infoValidation.apply(descriptor.getInfo())) {
      return null;
    }
    return resolveMethod.apply(BindingsTypeResolver.get(tag), descriptor.getInfo().name);
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

  @Override
  protected boolean addTypeFromResolveResult(String referenceName, ResolveResult resolveResult) {
    PsiElement psiElement = resolveResult.getElement();
    if (resolveResult instanceof Angular2ComponentPropertyResolveResult && psiElement != null) {
      myContext.setSource(psiElement);
      addType(((Angular2ComponentPropertyResolveResult)resolveResult).getJSType(), resolveResult.getElement(), false);
      return true;
    }
    return super.addTypeFromResolveResult(referenceName, resolveResult);
  }

  @Override
  protected boolean processFunction(@NotNull JSFunction function) {
    return Angular2LibrariesHacks.hackSlicePipeType(this, this.myContext, function)
           || super.processFunction(function);
  }

  @Override
  protected void processThisQualifierInExecutionScope(@NotNull JSThisExpression thisQualifier, PsiElement thisScope) {
    if (thisScope instanceof Angular2EmbeddedExpression) {
      TypeScriptClass componentClass = Angular2IndexingHandler.findComponentClass(thisQualifier);
      if (componentClass != null) {
        addType(componentClass.getJSType(), thisQualifier);
      }
      else {
        addType(JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS, true), thisQualifier);
      }
      return;
    }
    super.processThisQualifierInExecutionScope(thisQualifier, thisScope);
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

  @NotNull
  private static JSTypeSubstitutor intersectGenerics(@NotNull MultiMap<JSTypeSubstitutor.JSTypeGenericId, JSType> arguments,
                                                     @NotNull JSTypeSource source) {
    JSTypeSubstitutor result = new JSTypeSubstitutor();
    for (Map.Entry<JSTypeSubstitutor.JSTypeGenericId, Collection<JSType>> entry : arguments.entrySet()) {
      result.put(entry.getKey(), merge(source, filter(entry.getValue(), Objects::nonNull), false));
    }
    return result;
  }

  @NotNull
  private static JSType merge(@NotNull JSTypeSource source, @NotNull List<JSType> types, boolean union) {
    if (types.size() == 1) {
      return types.get(0);
    }
    return JSCompositeTypeImpl.optimizeTypeIfComposite(
      union ? new JSCompositeTypeImpl(source, types)
            : new JSIntersectionTypeImpl(source, types), OPTIMIZED_SIMPLE);
  }

  private static boolean isPropertyBindingAttribute(AttributeInfo info) {
    return info.type == BANANA_BOX_BINDING
           || (info.type == PROPERTY_BINDING
               && ((PropertyBindingInfo)info).bindingType == PROPERTY);
  }

  private static boolean isEventAttribute(AttributeInfo info) {
    return info.type == EVENT
           && ((Angular2AttributeNameParser.EventInfo)info).eventType == EventType.REGULAR;
  }

  private static class BindingsTypeResolver {

    @NotNull private final PsiElement myElement;
    @NotNull private final List<Angular2Directive> myMatched;
    @NotNull private final Angular2DeclarationsScope myScope;
    @Nullable private final JSType myRawTemplateContextType;
    @Nullable private final JSTypeSubstitutor myTypeSubstitutor;

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
          .filterKeys(Angular2TypeEvaluator::isPropertyBindingAttribute)
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

    @NotNull
    public static BindingsTypeResolver get(@NotNull XmlTag tag) {
      return CachedValuesManager.getCachedValue(tag, () -> CachedValueProvider.Result.create(
        new BindingsTypeResolver(tag), PsiModificationTracker.MODIFICATION_COUNT));
    }

    @NotNull
    public static BindingsTypeResolver get(@NotNull Angular2TemplateBindings bindings) {
      return CachedValuesManager.getCachedValue(bindings, () -> CachedValueProvider.Result.create(
        new BindingsTypeResolver(bindings), PsiModificationTracker.MODIFICATION_COUNT));
    }

    @Nullable
    public JSType resolveDirectiveEventType(@NotNull String name) {
      List<JSType> types = new SmartList<>();
      for (Angular2Directive directive : myMatched) {
        Angular2DirectiveProperty property;
        if (myScope.contains(directive)
            && (property = find(directive.getOutputs(), output -> output.getName().equals(name))) != null) {
          types.add(Angular2LibrariesHacks.hackNgModelChangeType(
            getEventVariableType(property.getType()), name));
        }
      }
      return processAndMerge(types);
    }

    @Nullable
    public JSType resolveDirectiveInputType(@NotNull String key) {
      List<JSType> types = new SmartList<>();
      for (Angular2Directive directive : myMatched) {
        Angular2DirectiveProperty property;
        if (myScope.contains(directive)
            && (property = find(directive.getInputs(), input -> input.getName().equals(key))) != null) {
          types.add(property.getType());
        }
      }
      return processAndMerge(types);
    }

    @Nullable
    public JSType resolveTemplateContextType() {
      return myTypeSubstitutor != null
             ? applyGenericArguments(myRawTemplateContextType, myTypeSubstitutor)
             : myRawTemplateContextType;
    }

    @Nullable
    private JSType processAndMerge(@NotNull List<JSType> types) {
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

    @NotNull
    private static <T extends PsiElement> Pair<JSType, JSTypeSubstitutor> analyze(@NotNull List<Angular2Directive> directives,
                                                                                  @NotNull T element,
                                                                                  @NotNull Function<T, EntryStream<String, JSExpression>> inputExpressionsProvider) {
      Map<String, JSExpression> inputsMap = inputExpressionsProvider
        .apply(element)
        .nonNullValues()
        .toMap((a, b) -> a);
      MultiMap<JSTypeSubstitutor.JSTypeGenericId, JSType> genericArguments = MultiMap.createSmart();
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
          JSTypeComparingContextService.getProcessingContextWithCache(clazz);
        directive.getInputs().forEach(property -> {
          JSExpression inputExpression = inputsMap.get(property.getName());
          JSType propertyType;
          if (inputExpression != null && (propertyType = property.getType()) != null) {
            JSLazyExpressionType inputType = new JSLazyExpressionType(inputExpression, true);
            if (isAnyType(getApparentType(inputType.getOriginalType()))) {
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
              JSGenericTypesEvaluatorBase.matchGenericTypes(new JSGenericMappings(genericArguments), processingContext,
                                                            inputType, propertyType);
              JSGenericTypesEvaluatorBase.widenInferredTypes(genericArguments, Collections.singletonList(propertyType), null, null);
            }
          }
        });
      });
      JSTypeSource typeSource = getTypeSource(element, templateContextTypes, genericArguments);
      return Pair.pair(
        typeSource == null || templateContextTypes.isEmpty() ? null : merge(typeSource, templateContextTypes, true),
        typeSource == null || genericArguments.isEmpty() ? null : intersectGenerics(genericArguments, typeSource));
    }
  }

  @Nullable
  private static JSTypeSource getTypeSource(@NotNull PsiElement element,
                                            @NotNull List<JSType> templateContextTypes,
                                            @NotNull MultiMap<JSTypeSubstitutor.JSTypeGenericId, JSType> genericArguments) {
    JSTypeSource source = getTypeSource(element, templateContextTypes);
    return source != null ? source : doIfNotNull(find(genericArguments.values(), Objects::nonNull), JSType::getSource);
  }

  @Nullable
  private static JSTypeSource getTypeSource(@NotNull PsiElement element,
                                            @NotNull List<JSType> types) {
    TypeScriptClass componentClass = Angular2IndexingHandler.findComponentClass(element);
    if (componentClass != null) {
      return JSTypeSourceFactory.createTypeSource(componentClass, true);
    }
    return doIfNotNull(getFirstItem(types), JSType::getSource);
  }
}
