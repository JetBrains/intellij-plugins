// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataFunction;
import org.angular2.index.Angular2MetadataFunctionIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static org.angular2.entities.Angular2ModuleResolver.NG_MODULE_PROP;
import static org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue;

public class Angular2MetadataFunctionStub extends Angular2MetadataElementStub<Angular2MetadataFunction> {


  public static Angular2MetadataFunctionStub createFunctionStub(@Nullable String memberName,
                                                                @NotNull JsonValue source,
                                                                @Nullable StubElement parent) {
    JsonObject sourceObject = (JsonObject)source;
    if (memberName != null && SYMBOL_FUNCTION.equals(readStringPropertyValue(sourceObject.findProperty(SYMBOL_TYPE)))) {
      JsonProperty value = sourceObject.findProperty(FUNCTION_VALUE);
      // so far we are interested only in functions, which would return NgModuleWithProviders
      JsonProperty ngModuleProp;
      if (value != null
          && value.getValue() instanceof JsonObject
          && (ngModuleProp = ((JsonObject)value.getValue()).findProperty(NG_MODULE_PROP)) != null
          && ngModuleProp.getValue() instanceof JsonObject
          && SYMBOL_REFERENCE.equals(readStringPropertyValue(((JsonObject)ngModuleProp.getValue()).findProperty(SYMBOL_TYPE)))) {
        return new Angular2MetadataFunctionStub(memberName, ngModuleProp.getValue(), parent);
      }
    }
    return null;
  }

  public Angular2MetadataFunctionStub(@NotNull StubInputStream stream, @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.FUNCTION);
  }

  public Angular2MetadataFunctionStub(@NotNull String memberName,
                                      @NotNull JsonValue ngModulePropValue,
                                      @Nullable StubElement parent) {
    super(memberName, parent, Angular2MetadataElementTypes.FUNCTION);
    Angular2MetadataReferenceStub.createReferenceStub(NG_MODULE_PROP, ngModulePropValue, this);
  }

  @Nullable
  public Angular2MetadataReferenceStub getNgModuleReference() {
    return (Angular2MetadataReferenceStub)getChildrenStubs().stream()
      .filter(child -> child instanceof Angular2MetadataReferenceStub
                       && NG_MODULE_PROP.equals(((Angular2MetadataReferenceStub)child).getMemberName()))
      .findFirst()
      .orElse(null);
  }

  @Override
  public void index(@NotNull IndexSink sink) {
    super.index(sink);
    if (getMemberName() != null) {
      sink.occurrence(Angular2MetadataFunctionIndex.KEY, getMemberName());
    }
  }
}
