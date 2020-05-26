// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.ecma6.JSTypeDeclaration;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptStringLiteralType;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.entities.*;
import org.angular2.entities.source.Angular2SourceDirectiveProperty;
import org.angular2.entities.source.Angular2SourceDirectiveVirtualProperty;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.openapi.vfs.VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS;
import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.codeInsight.Angular2LibrariesHacks.hackIonicComponentOutputs;
import static org.angular2.entities.metadata.Angular2MetadataUtil.getMetadataEntity;
import static org.angular2.entities.source.Angular2SourceDirective.getDirectiveKindNoCache;

public class Angular2IvyDirective extends Angular2IvyDeclaration<Angular2IvySymbolDef.Directive> implements Angular2Directive {

  private static final Key<Angular2DirectiveSelector> IVY_SELECTOR = new Key<>("ng.ivy.selector");
  private static final Key<List<String>> IVY_EXPORT_AS = new Key<>("ng.ivy.export-as");

  public Angular2IvyDirective(@NotNull Angular2IvySymbolDef.Directive entityDef) {
    super(entityDef);
  }

  @Override
  public @NotNull Angular2DirectiveSelector getSelector() {
    return getLazyValue(IVY_SELECTOR, () -> {
      TypeScriptStringLiteralType element = myEntityDef.getSelectorElement();
      if (element != null) {
        return createSelectorFromStringLiteralType(element);
      }
      return new Angular2DirectiveSelectorImpl(myEntityDef.getField(), null, null);
    });
  }

  @Override
  public @NotNull List<String> getExportAsList() {
    return getLazyValue(IVY_EXPORT_AS, () -> myEntityDef.getExportAsList());
  }

  @Override
  public @NotNull Collection<? extends Angular2DirectiveAttribute> getAttributes() {
    return getAttributes(myEntityDef);
  }

  private static Collection<? extends Angular2DirectiveAttribute> getAttributes(Angular2IvySymbolDef.Directive entityDef) {
    return CachedValuesManager.getCachedValue(entityDef.getField(), () -> {
      TypeScriptClass cls = entityDef.getContextClass();
      if (cls == null) {
        return create(Collections.emptyList(), entityDef.getField());
      }

      // find class with constructor
      Set<Object> dependencies = new HashSet<>();
      dependencies.add(cls);
      Ref<TypeScriptFunction> constructor = new Ref<>(ContainerUtil.find(cls.getConstructors(), fun -> !fun.isOverloadImplementation()));
      if (constructor.isNull()) {
        JSClassUtils.processClassesInHierarchy(cls, false, (aClass, typeSubstitutor, fromImplements) -> {
          dependencies.add(aClass);
          if (aClass instanceof TypeScriptClass) {
            constructor.set(ContainerUtil.find(((TypeScriptClass)aClass).getConstructors(), fun -> !fun.isOverloadImplementation()));
          }
          return constructor.isNull();
        });
        if (constructor.isNull()) {
          return create(Collections.emptyList(), dependencies.toArray());
        }
      }

      TypeScriptClass constructorClass = PsiTreeUtil.getContextOfType(constructor.get(), TypeScriptClass.class);
      if (constructorClass != null) {
        Map<String, JSTypeDeclaration> attributeNames =
          doIfNotNull(Angular2IvySymbolDef.getFactory(constructorClass), Angular2IvySymbolDef.Factory::getAttributeNames);
        if (attributeNames != null) {
          return create(
            ContainerUtil.map(attributeNames.entrySet(), entry -> new Angular2IvyDirectiveAttribute(entry.getKey(), entry.getValue())),
            dependencies.toArray());
        }
      }

      // Try to fallback to metadata JSON information - Angular 9.0.x case
      Angular2Directive metadataDirective = getMetadataDirective(cls);
      if (metadataDirective == null) {
        return create(Collections.emptyList(), cls, VFS_STRUCTURE_MODIFICATIONS);
      }
      return create(metadataDirective.getAttributes(), cls, metadataDirective);
    });
  }

  @Override
  public @NotNull Angular2DirectiveKind getDirectiveKind() {
    return getCachedValue(() -> create(
      getDirectiveKindNoCache(myClass), getClassModificationDependencies()));
  }

  protected static Angular2Directive getMetadataDirective(TypeScriptClass clazz) {
    return CachedValuesManager.getCachedValue(clazz, () -> {
      Angular2Directive metadataDirective = tryCast(getMetadataEntity(clazz), Angular2Directive.class);
      if (metadataDirective != null) {
        return create(metadataDirective, clazz, metadataDirective);
      }
      return create(null, clazz, VFS_STRUCTURE_MODIFICATIONS);
    });
  }

  protected Angular2DirectiveSelector createSelectorFromStringLiteralType(TypeScriptStringLiteralType type) {
    return new Angular2DirectiveSelectorImpl(type, type.getInnerText(), p -> new TextRange(1 + p.second, 1 + p.second + p.first.length()));
  }

  @Override
  public @NotNull Angular2DirectiveProperties getBindings() {
    return getCachedValue(
      () -> create(getPropertiesNoCache(),
                   getClassModificationDependencies())
    );
  }

  private @NotNull Angular2DirectiveProperties getPropertiesNoCache() {
    Map<String, Angular2DirectiveProperty> inputs = new LinkedHashMap<>();
    Map<String, Angular2DirectiveProperty> outputs = new LinkedHashMap<>();

    Map<String, String> inputMap = new LinkedHashMap<>();
    Map<String, String> outputMap = new LinkedHashMap<>();

    TypeScriptClass clazz = myClass;

    JSClassUtils.processClassesInHierarchy(clazz, false, (aClass, typeSubstitutor, fromImplements) -> {
      if (aClass instanceof TypeScriptClass) {
        Angular2IvySymbolDef.Entity entityDef = Angular2IvySymbolDef.get((TypeScriptClass)aClass, true);
        if (entityDef instanceof Angular2IvySymbolDef.Directive) {
          readMappingsInto((Angular2IvySymbolDef.Directive)entityDef, Angular2DecoratorUtil.INPUTS_PROP, inputMap);
          readMappingsInto((Angular2IvySymbolDef.Directive)entityDef, Angular2DecoratorUtil.OUTPUTS_PROP, outputMap);
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

    return new Angular2DirectiveProperties(inputs.values(), outputs.values());
  }

  private static void readMappingsInto(Angular2IvySymbolDef.Directive directiveDef, String field, Map<String, String> target) {
    directiveDef.readPropertyMappings(field).forEach((key, value) -> target.putIfAbsent(key, value));
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
