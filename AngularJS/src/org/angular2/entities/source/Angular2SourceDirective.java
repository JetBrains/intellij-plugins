// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.util.ObjectUtils;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.index.Angular2IndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.openapi.util.Pair.pair;

public class Angular2SourceDirective extends Angular2SourceDeclaration implements Angular2Directive {

  public Angular2SourceDirective(@NotNull ES6Decorator decorator, @NotNull JSImplicitElement implicitElement) {
    super(decorator, implicitElement);
  }

  @NotNull
  @Override
  public String getSelector() {
    return ObjectUtils.notNull(Angular2DecoratorUtil.getPropertyName(getDecorator(), Angular2DecoratorUtil.SELECTOR_PROP), "");
  }

  @Override
  public boolean isTemplate() {
    return getCachedValue(() -> CachedValueProvider.Result.create(
      Angular2IndexingHandler.isTemplate(getTypeScriptClass()), getTypeScriptClass()));
  }

  @Nullable
  @Override
  public String getExportAs() {
    return Angular2DecoratorUtil.getPropertyName(getDecorator(), Angular2DecoratorUtil.EXPORT_AS_PROP);
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
    List<Angular2DirectiveProperty> inputs = new ArrayList<>();
    List<Angular2DirectiveProperty> outputs = new ArrayList<>();

    Map<String, String> inputMap = readPropertyMappings(Angular2DecoratorUtil.INPUTS_PROP);
    Map<String, String> outputMap = readPropertyMappings(Angular2DecoratorUtil.OUTPUTS_PROP);

    TypeScriptTypeParser
      .buildTypeFromClass(getTypeScriptClass(), false)
      .getProperties()
      .forEach(prop -> {
        if (prop.getMemberSource().getSingleElement() instanceof JSAttributeListOwner) {
          processProperty(prop, (JSAttributeListOwner)prop.getMemberSource().getSingleElement(), inputMap, Angular2DecoratorUtil.INPUT_DEC,
                          inputs);
          processProperty(prop, (JSAttributeListOwner)prop.getMemberSource().getSingleElement(), outputMap,
                          Angular2DecoratorUtil.OUTPUT_DEC, outputs);
        }
      });

    return pair(Collections.unmodifiableCollection(inputs),
                Collections.unmodifiableCollection(outputs));
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

  private static void processProperty(@NotNull JSRecordType.PropertySignature property,
                                      @NotNull JSAttributeListOwner field,
                                      @NotNull Map<String, String> mappings,
                                      @NotNull String decorator,
                                      @NotNull List<Angular2DirectiveProperty> result) {
    String bindingName = mappings.get(property.getMemberName());
    if (bindingName == null && field.getAttributeList() != null) {
      bindingName = Arrays.stream(field.getAttributeList().getDecorators())
        .filter(d -> decorator.equals(d.getDecoratorName()))
        .findFirst()
        .map(d -> StringUtil.notNullize(getStringParamValue(d.getExpression()), property.getMemberName()))
        .orElse(null);
    }
    if (bindingName != null) {
      result.add(new Angular2SourceDirectiveProperty(property, bindingName));
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
}
