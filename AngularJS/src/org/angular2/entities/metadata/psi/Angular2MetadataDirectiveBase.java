// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.intellij.util.containers.ContainerUtil;
import one.util.streamex.EntryStream;
import org.angular2.entities.*;
import org.angular2.entities.metadata.stubs.Angular2MetadataDirectiveStubBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.util.ObjectUtils.notNull;

public abstract class Angular2MetadataDirectiveBase<Stub extends Angular2MetadataDirectiveStubBase<?>>
  extends Angular2MetadataDeclaration<Stub>
  implements Angular2Directive {

  private final AtomicNotNullLazyValue<List<String>> myExportAsList = new AtomicNotNullLazyValue<List<String>>() {
    @Override
    protected @NotNull List<String> compute() {
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

  @Override
  public @NotNull Angular2DirectiveSelector getSelector() {
    return mySelector.getValue();
  }

  @Override
  public @NotNull List<String> getExportAsList() {
    return myExportAsList.getValue();
  }

  @Override
  public @NotNull Collection<? extends Angular2DirectiveAttribute> getAttributes() {
    return myAttributes.getValue();
  }

  @Override
  protected @NotNull Result<Map<String, String>> resolveMappings(@NotNull String prop) {
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

  private @NotNull Collection<? extends Angular2DirectiveAttribute> buildAttributes() {
    return EntryStream.of(getStub().getAttributes())
      .mapKeyValue((name, index) -> new Angular2MetadataDirectiveAttribute(() -> getConstructorParameter(index),
                                                                           this::getSourceElement, name))
      .toImmutableList();
  }

  private @Nullable JSParameter getConstructorParameter(@NotNull Integer index) {
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
