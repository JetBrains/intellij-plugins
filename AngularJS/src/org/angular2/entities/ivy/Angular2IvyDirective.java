// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptLiteralType;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.entities.*;
import org.angular2.entities.source.Angular2SourceDirectiveProperty;
import org.angular2.entities.source.Angular2SourceDirectiveVirtualProperty;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.openapi.vfs.VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS;
import static org.angular2.codeInsight.Angular2LibrariesHacks.hackIonicComponentOutputs;
import static org.angular2.entities.ivy.Angular2IvyUtil.DIRECTIVE_DEF;
import static org.angular2.entities.source.Angular2SourceDirective.getConstructorParamsMatchNoCache;

public class Angular2IvyDirective extends Angular2IvyDeclaration implements Angular2Directive {

  public Angular2IvyDirective(@NotNull TypeScriptField defField) {
    super(defField);
  }

  protected Angular2IvyUtil.DirectiveDefKind getDefKind() {
    return DIRECTIVE_DEF;
  }

  @NotNull
  @Override
  public Angular2DirectiveSelector getSelector() {
    return CachedValuesManager.getCachedValue(myDefField, () -> {
      TypeScriptLiteralType element = getDefKind().getSelectorElement(myDefField);
      if (element != null) {
        return CachedValueProvider.Result.create(new Angular2DirectiveSelectorImpl(
          element, element.getInnerText(), p -> new TextRange(1 + p.second, 1 + p.second + p.first.length())), element);
      }
      return CachedValueProvider.Result.create(new Angular2DirectiveSelectorImpl(
        myDefField, null, null), myDefField);
    });
  }

  @NotNull
  @Override
  public List<String> getExportAsList() {
    return getDefKind().getExportAsList(myDefField);
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
    // TODO Angular 9 d.ts metadata lacks this information
    // Try to fallback to metadata JSON information
    return Optional.ofNullable(getMetadataDirective())
      .map(Angular2Directive::getAttributes)
      .orElseGet(Collections::emptyList);
  }

  @Override
  public boolean isStructuralDirective() {
    Pair<Boolean, Boolean> matches = getConstructorParamsMatch();
    return matches.first || matches.second;
  }

  @Override
  public boolean isRegularDirective() {
    return !getConstructorParamsMatch().first;
  }

  protected Angular2Directive getMetadataDirective() {
    TypeScriptClass clazz = myClass;
    return CachedValuesManager.getCachedValue(clazz, () -> {
      Angular2Directive metadataDirective = Angular2EntityObjectProvider.DIRECTIVE_PROVIDER.metadata.get(clazz);
      if (metadataDirective != null) {
        return CachedValueProvider.Result.create(metadataDirective, clazz, metadataDirective);
      }
      return CachedValueProvider.Result.create(null, clazz, VFS_STRUCTURE_MODIFICATIONS);
    });
  }

  @NotNull
  private Pair<Boolean, Boolean> getConstructorParamsMatch() {
    return getCachedValue(() -> CachedValueProvider.Result.create(
      getConstructorParamsMatchNoCache(myClass), getClassModificationDependencies()));
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

    Map<String, String> inputMap = new LinkedHashMap<>();
    Map<String, String> outputMap = new LinkedHashMap<>();

    TypeScriptClass clazz = myClass;

    JSClassUtils.processClassesInHierarchy(clazz, false, (aClass, typeSubstitutor, fromImplements) -> {
      if (aClass instanceof TypeScriptClass) {
        Pair<TypeScriptField, Angular2IvyUtil.EntityDefKind> defField = Angular2IvyUtil.findEntityDefField(aClass);
        if (defField != null && (defField.second instanceof Angular2IvyUtil.DirectiveDefKind)) {
          inputMap.putAll(((Angular2IvyUtil.DirectiveDefKind)defField.second)
                            .readPropertyMappings(defField.first, Angular2DecoratorUtil.INPUTS_PROP));
          outputMap.putAll(((Angular2IvyUtil.DirectiveDefKind)defField.second)
                             .readPropertyMappings(defField.first, Angular2DecoratorUtil.OUTPUTS_PROP));
        }
      }
      return true;
    });

    TypeScriptTypeParser
      .buildTypeFromClass(clazz, false)
      .getProperties()
      .forEach(prop -> {
        if (prop.getMemberSource().getSingleElement() != null) {
          processProperty(prop, inputMap, inputs);
          processProperty(prop, outputMap, outputs);
        }
      });

    hackIonicComponentOutputs(this, outputMap);

    inputMap.values().forEach(
      input -> inputs.put(input, new Angular2SourceDirectiveVirtualProperty(clazz, input)));
    outputMap.values().forEach(
      output -> outputs.put(output, new Angular2SourceDirectiveVirtualProperty(clazz, output)));

    return pair(Collections.unmodifiableCollection(inputs.values()),
                Collections.unmodifiableCollection(outputs.values()));
  }

  private static void processProperty(@NotNull JSRecordType.PropertySignature property,
                                      @NotNull Map<String, String> mappings,
                                      @NotNull Map<String, Angular2DirectiveProperty> result) {
    String bindingName = mappings.remove(property.getMemberName());
    if (bindingName != null) {
      result.putIfAbsent(bindingName, new Angular2SourceDirectiveProperty(property, bindingName));
    }
  }
}
