// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2DirectiveSelector;
import org.angular2.entities.Angular2DirectiveSelectorImpl;
import org.angular2.entities.metadata.stubs.Angular2MetadataDirectiveStubBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.openapi.util.Pair.pair;

public abstract class Angular2MetadataDirectiveBase<Stub extends Angular2MetadataDirectiveStubBase>
  extends Angular2MetadataDeclaration<Stub>
  implements Angular2Directive {

  public Angular2MetadataDirectiveBase(@NotNull Stub element) {
    super(element);
  }

  @NotNull
  @Override
  public Angular2DirectiveSelector getSelector() {
    return getCachedValue(() -> {
      Pair<TypeScriptClass, Collection<Object>> dependencies = getClassAndDependencies();
      return CachedValueProvider.Result.create(
        new Angular2DirectiveSelectorImpl(dependencies.first != null ? dependencies.first : this,
                                          getStub().getSelector(), a -> new TextRange(0, 0)),
        dependencies.second);
    });
  }

  @Nullable
  @Override
  public String getExportAs() {
    return getStub().getExportAs();
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
      () -> {
        Pair<TypeScriptClass, Collection<Object>> dependencies = getClassAndDependencies();
        return CachedValueProvider.Result.create(getProperties(dependencies.first), dependencies.second);
      }
    );
  }

  @SuppressWarnings("unchecked")
  private Pair<Collection<? extends Angular2DirectiveProperty>, Collection<? extends Angular2DirectiveProperty>> getProperties(
    @Nullable TypeScriptClass cls) {

    List<Angular2DirectiveProperty> inputs = new ArrayList<>();
    List<Angular2DirectiveProperty> outputs = new ArrayList<>();

    JSRecordType classType = cls != null
                             ? TypeScriptTypeParser.buildTypeFromClass(cls, false)
                             : null;

    collectProperties(getStub().getInputMappings(), classType, inputs);
    collectProperties(getStub().getOutputMappings(), classType, outputs);

    return pair(Collections.unmodifiableCollection(inputs),
                Collections.unmodifiableCollection(outputs));
  }

  private void collectProperties(Map<String, String> mappings, JSRecordType classType, List<Angular2DirectiveProperty> result) {
    mappings.forEach((String k, String v) -> result.add(createProperty(k, v, classType)));
  }

  private Angular2DirectiveProperty createProperty(@NotNull String fieldName,
                                                   @NotNull String bindingName,
                                                   @Nullable JSRecordType classType) {
    if (classType != null) {
      JSRecordType.PropertySignature sig = classType.findPropertySignature(fieldName);
      if (sig != null) {
        PsiElement source = sig.getMemberSource().getSingleElement();
        return new Angular2MetadataDirectiveProperty(sig, source != null ? source : getSourceElement(), bindingName);
      }
    }
    return new Angular2MetadataDirectiveProperty(null, getSourceElement(), bindingName);
  }
}
