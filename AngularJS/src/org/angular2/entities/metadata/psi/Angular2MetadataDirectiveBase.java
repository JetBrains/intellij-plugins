// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2DirectiveSelector;
import org.angular2.entities.Angular2DirectiveSelectorImpl;
import org.angular2.entities.metadata.stubs.Angular2MetadataClassStubBase;
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
    return getCachedClassBasedValue(
      cls -> new Angular2DirectiveSelectorImpl(cls != null ? cls : this,
                                               getStub().getSelector(), a -> new TextRange(0, 0)));
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
    return getCachedClassBasedValue(this::getProperties);
  }

  @SuppressWarnings("unchecked")
  private Pair<Collection<? extends Angular2DirectiveProperty>, Collection<? extends Angular2DirectiveProperty>> getProperties(
    @Nullable TypeScriptClass cls) {

    List<Angular2DirectiveProperty> inputs = new ArrayList<>();
    List<Angular2DirectiveProperty> outputs = new ArrayList<>();

    JSRecordType classType = cls != null
                             ? TypeScriptTypeParser.buildTypeFromClass(cls, false)
                             : null;

    Pair<Map<String, String>, Map<String, String>> mappings = getAllMappings();
    collectProperties(mappings.first, classType, inputs);
    collectProperties(mappings.second, classType, outputs);

    return pair(Collections.unmodifiableCollection(inputs),
                Collections.unmodifiableCollection(outputs));
  }

  @SuppressWarnings("unchecked")
  private Pair<Map<String, String>, Map<String, String>> getAllMappings() {
    Map<String, String> inputs = new HashMap<>();
    Map<String, String> outputs = new HashMap<>();
    Stack<Angular2MetadataClassBase<? extends Angular2MetadataClassStubBase>> classes = new Stack<>();
    Angular2MetadataClassBase<? extends Angular2MetadataClassStubBase> current = this;
    while (current != null) {
      classes.push(current);
      current = current.getExtendedClass();
    }
    while (!classes.isEmpty()) {
      current = classes.pop();
      inputs.putAll(current.getStub().getInputMappings());
      outputs.putAll(current.getStub().getOutputMappings());
    }
    return pair(inputs, outputs);
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
