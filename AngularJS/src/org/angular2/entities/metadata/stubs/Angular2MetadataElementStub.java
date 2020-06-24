// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.metadata.psi.MetadataElement;
import org.angular2.lang.metadata.psi.MetadataElementType;
import org.angular2.lang.metadata.stubs.MetadataElementStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

public abstract class Angular2MetadataElementStub<Psi extends MetadataElement> extends MetadataElementStub<Psi> {

  private static final AtomicNotNullLazyValue<Map<String, ConstructorFromJsonValue>> TYPE_FACTORY =
    new AtomicNotNullLazyValue<Map<String, ConstructorFromJsonValue>>() {
      @Override
      protected @NotNull Map<String, ConstructorFromJsonValue> compute() {
        return ContainerUtil.<String, ConstructorFromJsonValue>immutableMapBuilder()
          .put(SYMBOL_CLASS, Angular2MetadataClassStubBase::createClassStub)
          .put(SYMBOL_REFERENCE, Angular2MetadataReferenceStub::createReferenceStub)
          .put(SYMBOL_FUNCTION, Angular2MetadataFunctionStub::createFunctionStub)
          .put(SYMBOL_CALL, Angular2MetadataCallStub::createCallStub)
          .put(SYMBOL_SPREAD, Angular2MetadataSpreadStub::createSpreadStub)
          .put(STRING_TYPE, Angular2MetadataStringStub::new)
          .put(ARRAY_TYPE, Angular2MetadataArrayStub::new)
          .put(OBJECT_TYPE, Angular2MetadataObjectStub::new)
          .build();
      }
    };

  public Angular2MetadataElementStub(@Nullable String memberName, @Nullable StubElement parent, @NotNull MetadataElementType elementType) {
    super(memberName, parent, elementType);
  }

  public Angular2MetadataElementStub(@NotNull StubInputStream stream,
                                     @Nullable StubElement parent,
                                     @NotNull MetadataElementType elementType)
    throws IOException {
    super(stream, parent, elementType);
  }

  @Override
  protected Map<String, ConstructorFromJsonValue> getTypeFactory() {
    return TYPE_FACTORY.getValue();
  }
}
