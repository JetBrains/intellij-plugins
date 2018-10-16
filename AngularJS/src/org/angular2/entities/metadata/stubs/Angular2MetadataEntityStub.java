// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.entities.metadata.psi.Angular2MetadataEntity;
import org.angular2.index.Angular2MetadataEntityClassNameIndex;
import org.angular2.lang.metadata.psi.MetadataElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;

public class Angular2MetadataEntityStub<Psi extends Angular2MetadataEntity> extends Angular2MetadataClassStubBase<Psi> {

  protected static final String ARGUMENTS = "arguments";
  protected static final String NAME = "name";

  public Angular2MetadataEntityStub(@Nullable String memberName,
                                    @Nullable StubElement parent,
                                    @NotNull MetadataElementType elementType) {
    super(memberName, parent, elementType);
  }

  public Angular2MetadataEntityStub(@NotNull StubInputStream stream,
                                    @Nullable StubElement parent, @NotNull MetadataElementType elementType) throws IOException {
    super(stream, parent, elementType);
  }

  @Override
  public void index(@NotNull IndexSink sink) {
    super.index(sink);
    if (getClassName() != null) {
      sink.occurrence(Angular2MetadataEntityClassNameIndex.KEY, getClassName());
    }
  }

  protected static <T extends JsonValue> T getDecoratorArgument(@NotNull JsonObject decorator, Class<T> argClass) {
    JsonArray args = tryCast(doIfNotNull(decorator.findProperty(ARGUMENTS), JsonProperty::getValue), JsonArray.class);
    return args != null && args.getValueList().size() == 1 ? tryCast(args.getValueList().get(0), argClass) : null;
  }
}
