// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataElement;
import org.angular2.entities.metadata.psi.Angular2MetadataFunction;
import org.angular2.index.Angular2MetadataFunctionIndex;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue;

public class Angular2MetadataFunctionStub extends Angular2MetadataElementStub<Angular2MetadataFunction> {

  @NonNls private static final String VALUE_OBJ = "#value";

  public static Angular2MetadataFunctionStub createFunctionStub(@Nullable String memberName,
                                                                @NotNull JsonValue source,
                                                                @Nullable StubElement parent) {
    JsonObject sourceObject = (JsonObject)source;
    if (memberName != null && SYMBOL_FUNCTION.equals(readStringPropertyValue(sourceObject.findProperty(SYMBOL_TYPE)))) {
      JsonValue value = doIfNotNull(sourceObject.findProperty(FUNCTION_VALUE), JsonProperty::getValue);
      if (value != null) {
        return new Angular2MetadataFunctionStub(memberName, value, parent);
      }
    }
    return null;
  }

  public Angular2MetadataFunctionStub(@NotNull StubInputStream stream, @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.FUNCTION);
  }

  public Angular2MetadataFunctionStub(@NotNull String memberName,
                                      @NotNull JsonValue value,
                                      @Nullable StubElement parent) {
    super(memberName, parent, Angular2MetadataElementTypes.FUNCTION);
    createMember(VALUE_OBJ, value);
  }

  public @Nullable Angular2MetadataElementStub<? extends Angular2MetadataElement> getFunctionValue() {
    //noinspection unchecked
    return tryCast(findMember(VALUE_OBJ), Angular2MetadataElementStub.class);
  }

  @Override
  public void index(@NotNull IndexSink sink) {
    super.index(sink);
    if (getMemberName() != null) {
      sink.occurrence(Angular2MetadataFunctionIndex.KEY, getMemberName());
    }
  }
}
