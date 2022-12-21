// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.lang.javascript.psi.util.JSClassUtils;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.Stack;
import org.angular2.codeInsight.Angular2LibrariesHacks;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperties;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.metadata.stubs.Angular2MetadataClassStubBase;
import org.angular2.entities.metadata.stubs.Angular2MetadataReferenceStub;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.notNull;
import static org.angular2.Angular2DecoratorUtil.INPUTS_PROP;
import static org.angular2.Angular2DecoratorUtil.OUTPUTS_PROP;
import static org.angular2.web.Angular2WebSymbolsQueryConfigurator.KIND_NG_DIRECTIVE_INPUTS;
import static org.angular2.web.Angular2WebSymbolsQueryConfigurator.KIND_NG_DIRECTIVE_OUTPUTS;

public abstract class Angular2MetadataClassBase<Stub extends Angular2MetadataClassStubBase<?>> extends Angular2MetadataElement<Stub> {
  public Angular2MetadataClassBase(@NotNull Stub element) {
    super(element);
  }

  public @Nullable TypeScriptClass getTypeScriptClass() {
    return getClassAndDependencies().first;
  }

  @Override
  public @NotNull String getName() {
    return getCachedClassBasedValue(cls -> cls != null
                                           ? cls.getName()
                                           : StringUtil.notNullize(getStub().getMemberName(),
                                                                   Angular2Bundle.message("angular.description.unnamed")));
  }

  public Angular2MetadataClassBase<? extends Angular2MetadataClassStubBase<?>> getExtendedClass() {
    Angular2MetadataReferenceStub refStub = getStub().getExtendsReference();
    if (refStub != null) {
      //noinspection unchecked
      return ObjectUtils.tryCast(refStub.getPsi().resolve(), Angular2MetadataClassBase.class);
    }
    return null;
  }

  public @NotNull PsiElement getSourceElement() {
    return notNull(getTypeScriptClass(), this);
  }

  public @NotNull Angular2DirectiveProperties getBindings() {
    return CachedValuesManager.getCachedValue(this, this::getPropertiesNoCache);
  }

  protected Pair<TypeScriptClass, Collection<Object>> getClassAndDependencies() {
    return CachedValuesManager.getCachedValue(this, () -> {
      ProgressManager.checkCanceled();
      String className = getStub().getClassName();
      Angular2MetadataNodeModule nodeModule = getNodeModule();
      Pair<PsiFile, TypeScriptClass> fileAndClass = className != null && nodeModule != null
                                                    ? nodeModule.locateFileAndMember(className, TypeScriptClass.class)
                                                    : Pair.create(null, null);
      Collection<Object> dependencies = new HashSet<>();
      dependencies.add(getContainingFile());
      if (fileAndClass.second != null) {
        JSClassUtils.processClassesInHierarchy(fileAndClass.second, true, (aClass, typeSubstitutor, fromImplements) -> {
          dependencies.add(aClass.getContainingFile());
          return true;
        });
      }
      else if (fileAndClass.first != null) {
        dependencies.add(fileAndClass.first);
      }
      return Result.create(Pair.create(fileAndClass.second, dependencies), dependencies);
    });
  }

  protected <T> T getCachedClassBasedValue(Function<? super TypeScriptClass, ? extends T> provider) {
    return CachedValuesManager.getCachedValue(
      this,
      CachedValuesManager.getManager(getProject()).getKeyForClass(provider.getClass()),
      () -> {
        Pair<TypeScriptClass, Collection<Object>> dependencies = getClassAndDependencies();
        return Result.create(provider.apply(dependencies.first), dependencies.second);
      });
  }

  private Result<Angular2DirectiveProperties> getPropertiesNoCache() {
    Result<Pair<Map<String, String>, Map<String, String>>> mappings = getAllMappings();
    List<Angular2DirectiveProperty> inputs = collectProperties(mappings.getValue().first, KIND_NG_DIRECTIVE_INPUTS);
    List<Angular2DirectiveProperty> outputs = collectProperties(mappings.getValue().second, KIND_NG_DIRECTIVE_OUTPUTS);
    return Result.create(new Angular2DirectiveProperties(inputs, outputs),
                         mappings.getDependencyItems());
  }

  protected JSRecordType.PropertySignature getPropertySignature(String fieldName) {
    return doIfNotNull(getTypeScriptClass(), cls -> TypeScriptTypeParser.buildTypeFromClass(cls, false)
      .findPropertySignature(fieldName));
  }

  private Result<Pair<Map<String, String>, Map<String, String>>> getAllMappings() {
    Map<String, String> inputs = new HashMap<>();
    Map<String, String> outputs = new HashMap<>();
    Stack<Angular2MetadataClassBase<? extends Angular2MetadataClassStubBase<?>>> classes = new Stack<>();
    Angular2MetadataClassBase<? extends Angular2MetadataClassStubBase<?>> current = this;
    while (current != null) {
      classes.push(current);
      current = current.getExtendedClass();
    }
    if (this instanceof Angular2Directive) {
      Angular2LibrariesHacks.hackIonicComponentOutputs((Angular2Directive)this, outputs);
    }
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

  private List<Angular2DirectiveProperty> collectProperties(@NotNull Map<String, String> mappings, @NotNull String kind) {
    List<Angular2DirectiveProperty> result = new ArrayList<>();
    mappings.forEach((String fieldName, String bindingName) -> result.add(new Angular2MetadataDirectiveProperty(
      this, fieldName, bindingName, kind)));
    return result;
  }

  protected @NotNull Result<Map<String, String>> resolveMappings(@NotNull String prop) {
    return Result.create(Collections.emptyMap(), this);
  }
}
