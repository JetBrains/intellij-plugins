// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.entities.metadata.psi.Angular2MetadataEntity;
import org.angular2.lang.metadata.psi.MetadataElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Angular2MetadataEntityStub<Psi extends Angular2MetadataEntity> extends Angular2MetadataClassStubBase<Psi> {

  @NonNls protected static final String NAME = "name";
  @NonNls private static final String DECORATOR_FIELD_PREFIX = "___dec.";

  public Angular2MetadataEntityStub(@Nullable String memberName,
                                    @Nullable StubElement parent,
                                    @NotNull JsonObject source,
                                    @NotNull MetadataElementType elementType) {
    super(memberName, parent, source, elementType);
  }

  public Angular2MetadataEntityStub(@NotNull StubInputStream stream,
                                    @Nullable StubElement parent, @NotNull MetadataElementType elementType) throws IOException {
    super(stream, parent, elementType);
  }

  protected void stubDecoratorFields(@NotNull JsonObject initializer, String @NotNull ... fields) {
    for (String name : fields) {
      JsonProperty property = initializer.findProperty(name);
      if (property != null) {
        createMember(DECORATOR_FIELD_PREFIX + name, property.getValue());
      }
    }
  }

  public StubElement getDecoratorFieldValueStub(@NotNull String name) {
    return findMember(DECORATOR_FIELD_PREFIX + name);
  }
}
