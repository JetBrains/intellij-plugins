// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.codeInsight.refs.Angular2ReferenceExpressionResolver;
import org.angular2.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.openapi.util.Pair.pair;
import static org.angular2.entities.Angular2EntityUtils.TEMPLATE_REF;

public class Angular2SourceDirective extends Angular2SourceDeclaration implements Angular2Directive {

  public Angular2SourceDirective(@NotNull ES6Decorator decorator, @NotNull JSImplicitElement implicitElement) {
    super(decorator, implicitElement);
  }

  @NotNull
  @Override
  public Angular2DirectiveSelector getSelector() {
    return getCachedValue(() -> {
      JSProperty property = Angular2DecoratorUtil.getProperty(getDecorator(), Angular2DecoratorUtil.SELECTOR_PROP);
      if (property != null && property.getValue() instanceof JSLiteralExpression) {
        String value = ((JSLiteralExpression)property.getValue()).getStringValue();
        return CachedValueProvider.Result.create(new Angular2DirectiveSelectorImpl(
          property.getValue(), value, p -> new TextRange(1 + p.second, 1 + p.second + p.first.length())), property.getValue());
      }
      return CachedValueProvider.Result.create(new Angular2DirectiveSelectorImpl(getDecorator(), "", a -> new TextRange(0, 0)),
                                               getDecorator());
    });
  }

  @Override
  public boolean isTemplate() {
    return getCachedValue(() -> CachedValueProvider.Result.create(
      isTemplate(getTypeScriptClass()), getClassModificationDependencies()));
  }

  @NotNull
  @Override
  public List<String> getExportAsList() {
    return getCachedValue(() -> {
      String exportAsString = Angular2DecoratorUtil.getPropertyValue(getDecorator(), Angular2DecoratorUtil.EXPORT_AS_PROP);
      return CachedValueProvider.Result.create(exportAsString == null
                                               ? Collections.emptyList()
                                               : StringUtil.split(exportAsString, ","),
                                               getDecorator());
    });
  }

  @NotNull
  @Override
  public Collection<? extends Angular2DirectiveProperty> getInputs() {
    return getCachedProperties().first;
  }

  @NotNull
  @Override
  public Collection<? extends Angular2DirectiveProperty> getOutputs() {
    return getCachedProperties().second;
  }

  @NotNull
  private Pair<Collection<? extends Angular2DirectiveProperty>, Collection<? extends Angular2DirectiveProperty>> getCachedProperties() {
    return getCachedValue(
      () -> CachedValueProvider.Result.create(getProperties(),
                                              getClassModificationDependencies())
    );
  }

  @NotNull
  private Pair<Collection<? extends Angular2DirectiveProperty>, Collection<? extends Angular2DirectiveProperty>> getProperties() {
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

    inputMap.keySet().forEach(
      input -> inputs.put(input, new Angular2SourceDirectiveVirtualProperty(clazz, input)));
    outputMap.keySet().forEach(
      output -> outputs.put(output, new Angular2SourceDirectiveVirtualProperty(clazz, output)));

    return pair(Collections.unmodifiableCollection(inputs.values()),
                Collections.unmodifiableCollection(outputs.values()));
  }

  @NotNull
  private Map<String, String> readPropertyMappings(String source) {
    JSProperty prop = Angular2DecoratorUtil.getProperty(getDecorator(), source);
    if (prop != null && prop.getValue() instanceof JSArrayLiteralExpression) {
      return ((JSArrayLiteralExpression)prop.getValue())
        .getExpressionStream()
        .filter(expression -> expression instanceof JSLiteralExpression && ((JSLiteralExpression)expression).isQuotedLiteral())
        .map(expression -> ((JSLiteralExpression)expression).getStringValue())
        .filter(Objects::nonNull)
        .map(Angular2EntityUtils::parsePropertyMapping)
        .collect(Collectors.toMap(p -> p.first, p -> p.second, (a, b) -> a));
    }
    return Collections.emptyMap();
  }

  private static List<JSAttributeListOwner> getPropertySources(PsiElement property) {
    if (property instanceof TypeScriptFunction) {
      TypeScriptFunction fun = (TypeScriptFunction)property;
      if (!fun.isSetProperty() && !fun.isGetProperty()) {
        return Collections.emptyList();
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
        .map(d -> StringUtil.notNullize(getStringParamValue(d.getExpression()), property.getMemberName()))
        .orElse(null);
    }
    if (bindingName != null) {
      result.putIfAbsent(bindingName, new Angular2SourceDirectiveProperty(property, bindingName));
    }
  }

  @Nullable
  private static String getStringParamValue(@Nullable JSExpression expression) {
    if (expression instanceof JSCallExpression) {
      JSExpression[] args = ((JSCallExpression)expression).getArguments();
      if (args.length == 1 && args[0] instanceof JSLiteralExpression && ((JSLiteralExpression)args[0]).isQuotedLiteral()) {
        return ((JSLiteralExpression)args[0]).getStringValue();
      }
    }
    return null;
  }

  private static boolean isTemplate(@NotNull TypeScriptClass clazz) {
    return !JSClassUtils.processClassesInHierarchy(clazz, false, (aClass, typeSubstitutor, fromImplements) -> {
      if (aClass instanceof TypeScriptClass
          && Stream.of(((TypeScriptClass)aClass).getConstructors())
            .map(JSFunction::getParameterList)
            .filter(Objects::nonNull)
            .map(JSParameterList::getParameters)
            .flatMap(Stream::of)
            .map(JSParameterListElement::getType)
            .filter(Objects::nonNull)
            .map(type -> type.getTypeText())
            .anyMatch(t -> t.contains(TEMPLATE_REF))) {
        return false;
      }
      return true;
    });
  }
}
