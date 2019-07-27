// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataCall;
import org.angular2.entities.metadata.psi.Angular2MetadataElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue;

public class Angular2MetadataCallStub extends Angular2MetadataElementStub<Angular2MetadataCall> {

  @NonNls private static final String CALL_RESULT = "#expression";

  public static Angular2MetadataCallStub createCallStub(@Nullable String memberName,
                                                        @NotNull JsonValue source,
                                                        @Nullable StubElement parent) {
    JsonObject sourceObject = (JsonObject)source;
    if (SYMBOL_CALL.equals(readStringPropertyValue(sourceObject.findProperty(SYMBOL_TYPE)))) {
      JsonValue callResult = doIfNotNull(sourceObject.findProperty(EXPRESSION), JsonProperty::getValue);
      if (callResult != null) {
        return new Angular2MetadataCallStub(memberName, callResult, parent);
      }
    }
    return null;
  }

  private Angular2MetadataCallStub(@Nullable String memberName,
                                   @NotNull JsonValue callResult,
                                   @Nullable StubElement parent) {
    super(memberName, parent, Angular2MetadataElementTypes.CALL);
    createMember(CALL_RESULT, callResult);
  }

  public Angular2MetadataCallStub(@NotNull StubInputStream stream, @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.CALL);
  }

  public Angular2MetadataElementStub<? extends Angular2MetadataElement> getCallResult() {
    //noinspection unchecked
    return tryCast(findMember(CALL_RESULT), Angular2MetadataElementStub.class);
  }
}
