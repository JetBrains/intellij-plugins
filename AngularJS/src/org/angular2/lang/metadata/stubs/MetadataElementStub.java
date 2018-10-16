// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata.stubs;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.*;
import com.intellij.util.io.StringRef;
import org.angular2.lang.metadata.MetadataUtils;
import org.angular2.lang.metadata.psi.MetadataElement;
import org.angular2.lang.metadata.psi.MetadataElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

public abstract class MetadataElementStub<Psi extends MetadataElement> extends StubBase<Psi> {

  protected static final String __SYMBOLIC = "__symbolic";

  protected static final String ARRAY_TYPE = "#array";
  protected static final String OBJECT_TYPE = "#object";

  private StringRef myMemberName;

  public MetadataElementStub(@Nullable String memberName, @Nullable StubElement parent, @NotNull MetadataElementType elementType) {
    super(parent, elementType);
    myMemberName = StringRef.fromString(memberName);
  }

  public MetadataElementStub(@NotNull StubInputStream stream, @Nullable StubElement parent, @NotNull MetadataElementType elementType) throws IOException {
    super(parent, elementType);
    deserialize(stream);
  }

  @Nullable
  protected String getMemberName() {
    return StringRef.toString(myMemberName);
  }

  protected abstract Map<String, ConstructorFromJsonValue> getTypeFactory();

  public void deserialize(@NotNull StubInputStream stream) throws IOException {
    myMemberName = stream.readName();
  }

  public void serialize(@NotNull StubOutputStream stream) throws IOException {
    writeString(myMemberName, stream);
  }

  public void index(@NotNull IndexSink sink) {
  }

  protected void loadMemberProperty(JsonProperty p) {
    createMember(p.getName(), p.getValue());
  }

  protected void createMember(@Nullable String name, @Nullable JsonValue member) {
    ConstructorFromJsonValue constructor = null;
    if (member instanceof JsonArray) {
      constructor = getTypeFactory().get(ARRAY_TYPE);
    } else if (member instanceof JsonObject) {
      String type = MetadataUtils.readStringPropertyValue(((JsonObject)member).findProperty(__SYMBOLIC));
      constructor = getTypeFactory().get(type == null ? OBJECT_TYPE : type);
    }
    if (constructor != null) {
      constructor.construct(name, member, this);
    }
  }

  protected static void writeString(@Nullable StringRef ref, @NotNull final StubOutputStream dataStream) throws IOException {
    dataStream.writeName(StringRef.toString(ref));
  }

  protected interface ConstructorFromJsonValue {
    MetadataElementStub construct(@Nullable String memberName,
                                  @NotNull JsonValue source,
                                  @Nullable StubElement parent);
  }

}
