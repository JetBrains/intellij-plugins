// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Angular2MetadataObjectStub extends Angular2MetadataElementStub<Angular2MetadataObject> {
  public Angular2MetadataObjectStub(@Nullable String memberName, JsonValue source, @Nullable StubElement parent) {
    super(memberName, parent, Angular2MetadataElementTypes.OBJECT);
    ((JsonObject)source).getPropertyList().forEach(this::loadMemberProperty);
  }

  public Angular2MetadataObjectStub(@NotNull StubInputStream stream, @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.OBJECT);
  }
}
