// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.codeInsight.Angular2PipeUtil;
import org.angular2.entities.metadata.psi.Angular2MetadataClassBase;
import org.angular2.lang.metadata.MetadataUtils;
import org.angular2.lang.metadata.psi.MetadataElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;

public class Angular2MetadataClassStubBase<Psi extends Angular2MetadataClassBase> extends Angular2MetadataElementStub<Psi> {

  private static final String CALL = "call";
  private static final String NAME = "name";
  private static final String REFERENCE = "reference";
  private static final String EXPRESSION = "expression";

  public static Angular2MetadataClassStubBase<?> createClassStub(@Nullable String memberName,
                                                                 @NotNull JsonValue source,
                                                                 @Nullable StubElement parent) {
    JsonObject sourceClass = (JsonObject)source;
    JsonArray list = tryCast(doIfNotNull(sourceClass.findProperty("decorators"), JsonProperty::getValue), JsonArray.class);

    if (list == null) {
      return new Angular2MetadataClassStub(memberName, source, parent);
    }

    Function<JsonObject, Angular2MetadataClassStubBase> tryCreateClass = obj -> {
      if (Angular2PipeUtil.PIPE_DEC.equals(MetadataUtils.readStringPropertyValue(obj.findProperty(NAME)))) {
        return Angular2MetadataPipeStub.createPipeStub(memberName, parent, (JsonObject)obj.getParent().getParent());
      }
      return (Angular2MetadataClassStubBase)null;
    };

    return list.getValueList().stream()
      .map(v -> tryCast(v, JsonObject.class))
      .filter(obj -> obj != null
                     && CALL.equals(MetadataUtils.readStringPropertyValue(obj.findProperty(__SYMBOLIC))))
      .map(obj -> tryCast(doIfNotNull(obj.findProperty(EXPRESSION), JsonProperty::getValue), JsonObject.class))
      .filter(obj -> obj != null
                     && REFERENCE.equals(MetadataUtils.readStringPropertyValue(obj.findProperty(__SYMBOLIC))))
      .map(tryCreateClass)
      .filter(Objects::nonNull)
      .findFirst()
      .orElseGet(() -> new Angular2MetadataClassStub(memberName, source, parent));
  }

  public Angular2MetadataClassStubBase(@Nullable String memberName,
                                       @Nullable StubElement parent,
                                       @NotNull MetadataElementType elementType) {
    super(memberName, parent, elementType);
  }

  public Angular2MetadataClassStubBase(@NotNull StubInputStream stream,
                                       @Nullable StubElement parent, @NotNull MetadataElementType elementType) throws IOException {
    super(stream, parent, elementType);
  }

  @Nullable
  public String getClassName() {
    return getMemberName();
  }
}
