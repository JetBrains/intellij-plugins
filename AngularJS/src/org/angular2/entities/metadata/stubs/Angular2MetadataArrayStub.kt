// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Angular2MetadataArrayStub extends Angular2MetadataElementStub<Angular2MetadataArray> {

  public Angular2MetadataArrayStub(@NotNull StubInputStream stream, @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.ARRAY);
  }

  public Angular2MetadataArrayStub(@Nullable String memberName, @NotNull JsonValue source, @Nullable StubElement parent) {
    super(memberName, parent, Angular2MetadataElementTypes.ARRAY);
    ((JsonArray)source).getValueList().forEach(v -> createMember(null, v));
  }
}
