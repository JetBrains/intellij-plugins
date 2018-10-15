// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.util.ObjectUtils;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataClass;
import org.angular2.index.Angular2MetadataPipeIndex;
import org.angular2.lang.metadata.MetadataUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Angular2MetadataClassStub extends Angular2MetadataElementStub<Angular2MetadataClass> {

  public Angular2MetadataClassStub(@Nullable String memberName, @NotNull JsonValue source, @Nullable StubElement parent) {
    super(memberName, parent, Angular2MetadataElementTypes.CLASS);
    loadFromSource((JsonObject)source);
  }

  public Angular2MetadataClassStub(@NotNull StubInputStream stream, @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.CLASS);
  }

  public String getClassName() {
    return getMemberName();
  }

  @Override
  public void index(@NotNull IndexSink sink) {
    if (getMemberName() != null) {
      sink.occurrence(Angular2MetadataPipeIndex.KEY, getMemberName());
    }
  }

  private void loadFromSource(JsonObject source) {
    createDecorators(source);
    MetadataUtils
      .streamObjectProperty(source.findProperty("members"))
      .forEach(this::loadMemberProperty);
  }

  private void createDecorators(JsonObject source) {
    JsonArray list = ObjectUtils.tryCast(source.findProperty("decorators"), JsonArray.class);
    if (list != null) {
      list.getValueList().forEach(v -> createMember(null, v));
    }
  }

}
