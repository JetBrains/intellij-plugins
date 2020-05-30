// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.JSStubElementTypes;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl;
import com.intellij.lang.javascript.psi.stubs.ES6DecoratorStub;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.JSPropertyStub;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.util.AstLoadingFilter;
import com.intellij.util.ObjectUtils;
import one.util.streamex.StreamEx;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.codeInsight.refs.Angular2ReferenceExpressionResolver;
import org.angular2.entities.*;
import org.angular2.entities.ivy.Angular2IvyEntity;
import org.angular2.entities.metadata.Angular2MetadataUtil;
import org.angular2.index.Angular2IndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.containers.ContainerUtil.exists;
import static org.angular2.entities.Angular2EntitiesProvider.withJsonMetadataFallback;
import static org.angular2.entities.Angular2EntityUtils.*;
import static org.angular2.entities.ivy.Angular2IvyUtil.getIvyEntity;

public class Angular2SourceDirective extends Angular2SourceDeclaration implements Angular2Directive {

  public Angular2SourceDirective(@NotNull ES6Decorator decorator, @NotNull JSImplicitElement implicitElement) {
    super(decorator, implicitElement);
  }

  @Override
  public @NotNull Angular2DirectiveSelector getSelector() {
    return getCachedValue(() -> {
      JSProperty property = Angular2DecoratorUtil.getProperty(getDecorator(), Angular2DecoratorUtil.SELECTOR_PROP);
      String value = null;
      if (property != null) {
        JSLiteralExpression initializer;
        JSPropertyStub stub = ((JSPropertyImpl)property).getStub();
        if (stub != null) {
          initializer = StreamEx.of(stub.getChildrenStubs())
            .map(StubElement::getPsi)
            .select(JSLiteralExpression.class)
            .findFirst()
            .orElse(null);
          value = initializer == null ? null : doIfNotNull(initializer.getSignificantValue(), Angular2EntityUtils::unquote);
        }
        else {
          initializer = ObjectUtils.tryCast(property.getValue(), JSLiteralExpression.class);
          value = initializer == null ? null : initializer.getStringValue();
        }
        if (value != null) {
          return CachedValueProvider.Result.create(new Angular2DirectiveSelectorImpl(
            initializer, StringUtil.unquoteString(value), p -> new TextRange(1 + p.second, 1 + p.second + p.first.length())), property);
        }
        value = AstLoadingFilter.forceAllowTreeLoading(property.getContainingFile(),
                                                       () -> Angular2DecoratorUtil.getExpressionStringValue(property.getValue()));
      }
      return CachedValueProvider.Result.create(new Angular2DirectiveSelectorImpl(getDecorator(), value, null),
                                               getDecorator());
    });
  }

  @Override
  public @NotNull Angular2DirectiveKind getDirectiveKind() {
    return getCachedValue(() -> CachedValueProvider.Result.create(
      getDirectiveKindNoCache(myClass), getClassModificationDependencies()));
  }

  @Override
  public @NotNull List<String> getExportAsList() {
    return getCachedValue(() -> {
      String exportAsString = Angular2DecoratorUtil.getPropertyValue(getDecorator(), Angular2DecoratorUtil.EXPORT_AS_PROP);
      return CachedValueProvider.Result.create(exportAsString == null
                                               ? Collections.emptyList()
                                               : StringUtil.split(exportAsString, ","),
                                               getDecorator());
    });
  }

  @Override
  public @NotNull Collection<? extends Angular2DirectiveAttribute> getAttributes() {
    return getCachedValue(
      () -> Result.create(getAttributeParameters(),
                          getClassModificationDependencies())
    );
  }

  @Override
  public @NotNull Angular2DirectiveProperties getBindings() {
    return getCachedValue(
      () -> CachedValueProvider.Result.create(getPropertiesNoCache(),
                                              getClassModificationDependencies())
    );
  }

  private @NotNull Angular2DirectiveProperties getPropertiesNoCache() {
    Map<String, Angular2DirectiveProperty> inputs = new LinkedHashMap<>();
    Map<String, Angular2DirectiveProperty> outputs = new LinkedHashMap<>();

    Map<String, String> inputMap = readPropertyMappings(Angular2DecoratorUtil.INPUTS_PROP);
    Map<String, String> outputMap = readPropertyMappings(Angular2DecoratorUtil.OUTPUTS_PROP);

    TypeScriptClass clazz = getTypeScriptClass();

    TypeScriptTypeParser
      .buildTypeFromClass(clazz, false)
      .getProperties()
      .forEach(prop -> {
        for (JSAttributeListOwner el : getPropertySources(prop.getMemberSource().getSingleElement())) {
          processProperty(prop, el, inputMap, Angular2DecoratorUtil.INPUT_DEC, inputs);
          processProperty(prop, el, outputMap, Angular2DecoratorUtil.OUTPUT_DEC, outputs);
        }
      });

    inputMap.values().forEach(
      input -> inputs.put(input, new Angular2SourceDirectiveVirtualProperty(clazz, input)));
    outputMap.values().forEach(
      output -> outputs.put(output, new Angular2SourceDirectiveVirtualProperty(clazz, output)));

    Ref<Angular2DirectiveProperties> inheritedProperties = new Ref<>();
    JSClassUtils.processClassesInHierarchy(clazz, false, (aClass, typeSubstitutor, fromImplements) -> {
      if (aClass instanceof TypeScriptClass && Angular2EntitiesProvider.isDeclaredClass((TypeScriptClass)aClass)) {
        Angular2DirectiveProperties props = withJsonMetadataFallback(aClass, cls -> {
          Angular2IvyEntity<?> ivyEntity = getIvyEntity(aClass, true);
          if (ivyEntity instanceof Angular2Directive) {
            return ((Angular2Directive)ivyEntity).getBindings();
          }
          return null;
        }, Angular2MetadataUtil::getMetadataClassDirectiveProperties);
        if (props != null) {
          inheritedProperties.set(props);
          return false;
        }
      }
      return true;
    });

    if (!inheritedProperties.isNull()) {
      inheritedProperties.get().getInputs().forEach(prop -> inputs.putIfAbsent(prop.getName(), prop));
      inheritedProperties.get().getOutputs().forEach(prop -> outputs.putIfAbsent(prop.getName(), prop));
    }
    return new Angular2DirectiveProperties(inputs.values(), outputs.values());
  }

  private @NotNull Map<String, String> readPropertyMappings(String source) {
    JSProperty prop = Angular2DecoratorUtil.getProperty(getDecorator(), source);
    if (prop == null) {
      return Collections.emptyMap();
    }
    JSPropertyStub stub = ((JSPropertyImpl)prop).getStub();
    Stream<String> stream;
    if (stub != null) {
      stream = StreamEx.of(stub.getChildrenStubs())
        .map(StubElement::getPsi)
        .select(JSLiteralExpression.class)
        .map(JSLiteralExpression::getSignificantValue)
        .nonNull()
        .map(Angular2EntityUtils::unquote);
    }
    else if (prop.getValue() instanceof JSArrayLiteralExpression) {
      stream = ((JSArrayLiteralExpression)prop.getValue())
        .getExpressionStream()
        .filter(expression -> expression instanceof JSLiteralExpression && ((JSLiteralExpression)expression).isQuotedLiteral())
        .map(expression -> ((JSLiteralExpression)expression).getStringValue())
        .filter(Objects::nonNull);
    }
    else {
      return Collections.emptyMap();
    }
    return stream
      .map(Angular2EntityUtils::parsePropertyMapping)
      .collect(Collectors.toMap(p -> p.first, p -> p.second, (a, b) -> a));
  }

  private static List<JSAttributeListOwner> getPropertySources(PsiElement property) {
    if (property instanceof TypeScriptFunction) {
      TypeScriptFunction fun = (TypeScriptFunction)property;
      if (!fun.isSetProperty() && !fun.isGetProperty()) {
        return Collections.singletonList(fun);
      }
      List<JSAttributeListOwner> result = new ArrayList<>();
      result.add(fun);
      Angular2ReferenceExpressionResolver.findPropertyAccessor(fun, fun.isGetProperty(), result::add);
      return result;
    }
    else if (property instanceof JSAttributeListOwner) {
      return Collections.singletonList((JSAttributeListOwner)property);
    }
    return Collections.emptyList();
  }

  private static void processProperty(@NotNull JSRecordType.PropertySignature property,
                                      @NotNull JSAttributeListOwner field,
                                      @NotNull Map<String, String> mappings,
                                      @NotNull String decorator,
                                      @NotNull Map<String, Angular2DirectiveProperty> result) {
    String bindingName = mappings.remove(property.getMemberName());
    if (bindingName == null && field.getAttributeList() != null) {
      bindingName = Arrays.stream(field.getAttributeList().getDecorators())
        .filter(d -> decorator.equals(d.getDecoratorName()))
        .findFirst()
        .map(d -> StringUtil.notNullize(getStringParamValue(d), property.getMemberName()))
        .orElse(null);
    }
    if (bindingName != null) {
      result.putIfAbsent(bindingName, new Angular2SourceDirectiveProperty(property, bindingName));
    }
  }

  private @NotNull Collection<? extends Angular2DirectiveAttribute> getAttributeParameters() {
    final TypeScriptFunction[] constructors = myClass.getConstructors();
    return constructors.length == 1
           ? processCtorParameters(constructors[0])
           : Arrays.stream(constructors)
             .filter(TypeScriptFunction::isOverloadImplementation)
             .findFirst()
             .map(Angular2SourceDirective::processCtorParameters)
             .orElse(Collections.emptyList());
  }

  private static @NotNull Collection<? extends Angular2DirectiveAttribute> processCtorParameters(final @NotNull JSFunction ctor) {
    return StreamEx.of(ctor.getParameterVariables())
      .mapToEntry(JSParameter::getAttributeList)
      .nonNullValues()
      .flatMapValues(a -> Arrays.stream(a.getDecorators()))
      .filterValues(d -> Angular2DecoratorUtil.ATTRIBUTE_DEC.equals(d.getDecoratorName()))
      .mapValues(Angular2SourceDirective::getStringParamValue)
      .filterValues(v -> v != null && !v.trim().isEmpty())
      .distinctValues()
      .mapToValue(Angular2SourceDirectiveAttribute::new)
      .values()
      .toList();
  }

  private static @Nullable String getStringParamValue(@Nullable ES6Decorator decorator) {
    if (decorator == null || !Angular2IndexingHandler.isDecoratorStringArgStubbed(decorator)) {
      return null;
    }
    ES6DecoratorStub stub = decorator.getStub();
    if (stub != null) {
      return StreamEx.of(stub.getChildrenStubs())
        .filter(s -> s.getStubType() == JSStubElementTypes.CALL_EXPRESSION)
        .map(StubElement::getPsi)
        .select(JSCallExpression.class)
        .map(JSStubBasedPsiTreeUtil::findRequireCallArgument)
        .nonNull()
        .map(JSLiteralExpression::getSignificantValue)
        .nonNull()
        .map(Angular2EntityUtils::unquote)
        .findFirst()
        .orElse(null);
    }
    JSCallExpression expression = ObjectUtils.tryCast(decorator.getExpression(), JSCallExpression.class);
    if (expression != null) {
      JSExpression[] args = expression.getArguments();
      if (args.length == 1 && args[0] instanceof JSLiteralExpression && ((JSLiteralExpression)args[0]).isQuotedLiteral()) {
        return ((JSLiteralExpression)args[0]).getStringValue();
      }
    }
    return null;
  }

  public static @NotNull Angular2DirectiveKind getDirectiveKindNoCache(@NotNull TypeScriptClass clazz) {
    Ref<Angular2DirectiveKind> result = new Ref<>(null);
    JSClassUtils.processClassesInHierarchy(clazz, false, (aClass, typeSubstitutor, fromImplements) -> {
      if (aClass instanceof TypeScriptClass) {
        List<String> types = StreamEx.of(((TypeScriptClass)aClass).getConstructors())
          .map(JSFunction::getParameterList)
          .nonNull()
          .flatArray(JSParameterList::getParameters)
          .map(JSParameterListElement::getJSType)
          .nonNull()
          .map(type -> type.getTypeText())
          .toList();
        result.set(Angular2DirectiveKind.get(
          exists(types, t -> t.contains(ELEMENT_REF)),
          exists(types, t -> t.contains(TEMPLATE_REF)),
          exists(types, t -> t.contains(VIEW_CONTAINER_REF))
        ));
      }
      return result.isNull();
    });
    return result.isNull() ? Angular2DirectiveKind.REGULAR : result.get();
  }
}
