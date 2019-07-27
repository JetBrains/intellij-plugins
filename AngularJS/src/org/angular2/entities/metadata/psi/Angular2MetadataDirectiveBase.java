// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.containers.ContainerUtil;
import one.util.streamex.EntryStream;
import org.angular2.codeInsight.Angular2LibrariesHacks;
import org.angular2.entities.*;
import org.angular2.entities.metadata.stubs.Angular2MetadataClassStubBase;
import org.angular2.entities.metadata.stubs.Angular2MetadataDirectiveStubBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.notNull;
import static org.angular2.Angular2DecoratorUtil.INPUTS_PROP;
import static org.angular2.Angular2DecoratorUtil.OUTPUTS_PROP;

public abstract class Angular2MetadataDirectiveBase<Stub extends Angular2MetadataDirectiveStubBase<?>>
  extends Angular2MetadataDeclaration<Stub>
  implements Angular2Directive {

  private final AtomicNotNullLazyValue<List<String>> myExportAsList = new AtomicNotNullLazyValue<List<String>>() {
    @NotNull
    @Override
    protected List<String> compute() {
      String exportAsString = getStub().getExportAs();
      return exportAsString == null
             ? Collections.emptyList()
             : StringUtil.split(exportAsString, ",");
    }
  };
  private final AtomicNotNullLazyValue<Angular2DirectiveSelector> mySelector = AtomicNotNullLazyValue.createValue(
    () -> new Angular2DirectiveSelectorImpl(() -> notNull(getTypeScriptClass(), this), getStub().getSelector(), null)
  );
  private final AtomicNotNullLazyValue<Collection<? extends Angular2DirectiveAttribute>> myAttributes = AtomicNotNullLazyValue.createValue(
    this::buildAttributes
  );

  public Angular2MetadataDirectiveBase(@NotNull Stub element) {
    super(element);
  }

  @NotNull
  @Override
  public Angular2DirectiveSelector getSelector() {
    return mySelector.getValue();
  }

  @NotNull
  @Override
  public List<String> getExportAsList() {
    return myExportAsList.getValue();
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
  @Override
  public Collection<? extends Angular2DirectiveAttribute> getAttributes() {
    return myAttributes.getValue();
  }

  @NotNull
  private Pair<Collection<? extends Angular2DirectiveProperty>, Collection<? extends Angular2DirectiveProperty>> getCachedProperties() {
    return CachedValuesManager.getCachedValue(this, this::getProperties);
  }

  private Result<Pair<Collection<? extends Angular2DirectiveProperty>, Collection<? extends Angular2DirectiveProperty>>> getProperties() {
    Result<Pair<Map<String, String>, Map<String, String>>> mappings = getAllMappings();
    List<Angular2DirectiveProperty> inputs = collectProperties(mappings.getValue().first);
    List<Angular2DirectiveProperty> outputs = collectProperties(mappings.getValue().second);
    return Result.create(pair(inputs, outputs),
                         mappings.getDependencyItems());
  }

  private JSRecordType.PropertySignature getPropertySignature(String fieldName) {
    return doIfNotNull(getTypeScriptClass(), cls -> TypeScriptTypeParser.buildTypeFromClass(cls, false)
      .findPropertySignature(fieldName));
  }

  private Result<Pair<Map<String, String>, Map<String, String>>> getAllMappings() {
    Map<String, String> inputs = new HashMap<>();
    Map<String, String> outputs = new HashMap<>();
    Stack<Angular2MetadataClassBase<? extends Angular2MetadataClassStubBase>> classes = new Stack<>();
    Angular2MetadataClassBase<? extends Angular2MetadataClassStubBase> current = this;
    while (current != null) {
      classes.push(current);
      current = current.getExtendedClass();
    }
    Angular2LibrariesHacks.hackIonicComponentOutputs(this, outputs);
    while (!classes.isEmpty()) {
      current = classes.pop();
      inputs.putAll(current.getStub().getInputMappings());
      outputs.putAll(current.getStub().getOutputMappings());
    }
    Set<Object> cacheDependencies = new HashSet<>();
    BiConsumer<Map<String, String>, String> collectAdditionalMappings = (map, prop) -> {
      Result<Map<String, String>> mappings = resolveMappings(prop);
      map.putAll(mappings.getValue());
      ContainerUtil.addAll(cacheDependencies, mappings.getDependencyItems());
    };
    collectAdditionalMappings.accept(inputs, INPUTS_PROP);
    collectAdditionalMappings.accept(outputs, OUTPUTS_PROP);
    return Result.create(pair(inputs, outputs), cacheDependencies);
  }

  private List<Angular2DirectiveProperty> collectProperties(@NotNull Map<String, String> mappings) {
    List<Angular2DirectiveProperty> result = new ArrayList<>();
    mappings.forEach((String fieldName, String bindingName) -> result.add(new Angular2MetadataDirectiveProperty(
      () -> getPropertySignature(fieldName), this::getSourceElement, bindingName)));
    return Collections.unmodifiableList(result);
  }

  @NotNull
  private Result<Map<String, String>> resolveMappings(@NotNull String prop) {
    StubElement propertyStub = getStub().getDecoratorFieldValueStub(prop);
    if (propertyStub == null) {
      return Result.create(Collections.emptyMap(), this);
    }
    Map<String, String> result = new HashMap<>();
    Set<PsiElement> cacheDependencies = new HashSet<>();
    collectReferencedElements(propertyStub.getPsi(), element -> {
      if (element instanceof Angular2MetadataString) {
        Pair<String, String> p = Angular2EntityUtils.parsePropertyMapping(((Angular2MetadataString)element).getValue());
        result.putIfAbsent(p.first, p.second);
      }
    }, cacheDependencies);
    return Result.create(result, cacheDependencies);
  }

  @NotNull
  private Collection<? extends Angular2DirectiveAttribute> buildAttributes() {
    return EntryStream.of(getStub().getAttributes())
      .mapKeyValue((name, index) -> new Angular2MetadataDirectiveAttribute(() -> getConstructorParameter(index),
                                                                           this::getSourceElement, name))
      .toImmutableList();
  }

  @Nullable
  private JSParameter getConstructorParameter(@NotNull Integer index) {
    TypeScriptClass cls = getTypeScriptClass();
    if (cls == null || index < 0) {
      return null;
    }
    final TypeScriptFunction[] constructors = cls.getConstructors();
    JSFunction ctor = constructors.length == 1
                      ? constructors[0]
                      : ContainerUtil.find(constructors, TypeScriptFunction::isOverloadImplementation);
    if (ctor == null) {
      return null;
    }
    final JSParameter[] parameters = ctor.getParameterVariables();
    return index < parameters.length ? parameters[index] : null;
  }
}
