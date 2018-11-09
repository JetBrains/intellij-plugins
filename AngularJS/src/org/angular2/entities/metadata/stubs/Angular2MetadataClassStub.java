// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Angular2MetadataClassStub extends Angular2MetadataClassStubBase<Angular2MetadataClass> {

  public Angular2MetadataClassStub(@Nullable String memberName, @NotNull JsonValue source, @Nullable StubElement parent) {
    super(memberName, parent, (JsonObject)source, Angular2MetadataElementTypes.CLASS);
  }

  public Angular2MetadataClassStub(@NotNull StubInputStream stream, @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.CLASS);
  }
}
