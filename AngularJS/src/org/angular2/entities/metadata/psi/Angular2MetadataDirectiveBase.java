// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import org.angular2.codeInsight.Angular2LibrariesHacks;
import org.angular2.entities.*;
import org.angular2.entities.metadata.stubs.Angular2MetadataClassStubBase;
import org.angular2.entities.metadata.stubs.Angular2MetadataDirectiveStubBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.openapi.util.Pair.pair;
import static org.angular2.Angular2DecoratorUtil.INPUTS_PROP;
import static org.angular2.Angular2DecoratorUtil.OUTPUTS_PROP;

public abstract class Angular2MetadataDirectiveBase<Stub extends Angular2MetadataDirectiveStubBase<?>>
  extends Angular2MetadataDeclaration<Stub>
  implements Angular2Directive {

  private final AtomicNotNullLazyValue<List<String>> exportAsList = new AtomicNotNullLazyValue<List<String>>() {
    @NotNull
    @Override
    protected List<String> compute() {
      String exportAsString = getStub().getExportAs();
      return exportAsString == null
             ? Collections.emptyList()
             : StringUtil.split(exportAsString, ",");
    }
  };

  public Angular2MetadataDirectiveBase(@NotNull Stub element) {
    super(element);
  }

  @NotNull
  @Override
  public Angular2DirectiveSelector getSelector() {
    return getCachedClassBasedValue(
      cls -> new Angular2DirectiveSelectorImpl(cls != null ? cls : this,
                                               getStub().getSelector(),
                                               a -> new TextRange(0, 0)));
  }

  @NotNull
  @Override
  public List<String> getExportAsList() {
    return exportAsList.getValue();
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

  private Pair<Map<String, String>, Map<String, String>> getAllMappings() {
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
    inputs.putAll(resolveMappings(INPUTS_PROP));
    outputs.putAll(resolveMappings(OUTPUTS_PROP));
    return pair(inputs, outputs);
  }

  private void collectProperties(Map<String, String> mappings, JSRecordType classType, List<? super Angular2DirectiveProperty> result) {
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

  @NotNull
  private Map<String, String> resolveMappings(@NotNull String prop) {
    StubElement propertyStub = getStub().getDecoratorFieldValueStub(prop);
    if (propertyStub == null) {
      return Collections.emptyMap();
    }
    Map<String, String> result = new HashMap<>();
    collectReferencedElements(propertyStub.getPsi(), element -> {
      if (element instanceof Angular2MetadataString) {
        Pair<String, String> p = Angular2EntityUtils.parsePropertyMapping(((Angular2MetadataString)element).getValue());
        result.putIfAbsent(p.first, p.second);
      }
    });
    return result;
  }
}
