// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

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

import static com.intellij.openapi.util.Pair.pair;

public abstract class Angular2MetadataElementStub<Psi extends MetadataElement> extends MetadataElementStub<Psi> {

  private static Map<String, ConstructorFromJsonValue> TYPE_FACTORY;

  private static Map<String, ConstructorFromJsonValue> getTypeFactory_() {
    if (TYPE_FACTORY == null) {
      TYPE_FACTORY = ContainerUtil.newHashMap(
        pair("class", Angular2MetadataClassStubBase::createClassStub),
        pair("reference", Angular2MetadataReferenceStub::new),
        pair(ARRAY_TYPE, Angular2MetadataArrayStub::new),
        pair(OBJECT_TYPE, Angular2MetadataObjectStub::new)
      );
    }
    return TYPE_FACTORY;
  }

  public Angular2MetadataElementStub(@Nullable String memberName, @Nullable StubElement parent, @NotNull MetadataElementType elementType) {
    super(memberName, parent, elementType);
  }

  public Angular2MetadataElementStub(@NotNull StubInputStream stream, @Nullable StubElement parent, @NotNull MetadataElementType elementType)
    throws IOException {
    super(stream, parent, elementType);
  }

  @Override
  protected Map<String, ConstructorFromJsonValue> getTypeFactory() {
    return getTypeFactory_();
  }
}
