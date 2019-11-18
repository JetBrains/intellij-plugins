// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
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
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.codeInsight.Angular2LibrariesHacks.hackIonicComponentOutputs;
import static org.angular2.entities.source.Angular2SourceDirective.getConstructorParamsMatchNoCache;

public class Angular2IvyDirective extends Angular2IvyDeclaration<Angular2IvyEntityDef.Directive> implements Angular2Directive {

  public Angular2IvyDirective(@NotNull Angular2IvyEntityDef.Directive entityDef) {
    super(entityDef);
  }

  @NotNull
  @Override
  public Angular2DirectiveSelector getSelector() {
    Angular2IvyEntityDef.Directive entityDef = myEntityDef;
    return CachedValuesManager.getCachedValue(entityDef.getField(), () -> {
      TypeScriptLiteralType element = entityDef.getSelectorElement();
      if (element != null) {
        return CachedValueProvider.Result.create(new Angular2DirectiveSelectorImpl(
          element, element.getInnerText(), p -> new TextRange(1 + p.second, 1 + p.second + p.first.length())), element);
      }
      return CachedValueProvider.Result.create(new Angular2DirectiveSelectorImpl(
        entityDef.getField(), null, null), entityDef.getField());
    });
  }

  @NotNull
  @Override
  public List<String> getExportAsList() {
    return myEntityDef.getExportAsList();
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
      Angular2Directive metadataDirective = tryCast(Angular2EntitiesProvider.getMetadataEntity(clazz), Angular2Directive.class);
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
        Angular2IvyEntityDef entityDef = Angular2IvyEntityDef.get((TypeScriptClass)aClass);
        if (entityDef instanceof Angular2IvyEntityDef.Directive) {
          inputMap.putAll(((Angular2IvyEntityDef.Directive)entityDef)
                            .readPropertyMappings(Angular2DecoratorUtil.INPUTS_PROP));
          outputMap.putAll(((Angular2IvyEntityDef.Directive)entityDef)
                             .readPropertyMappings(Angular2DecoratorUtil.OUTPUTS_PROP));
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
