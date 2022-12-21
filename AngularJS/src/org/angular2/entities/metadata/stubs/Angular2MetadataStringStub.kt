// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public class Angular2MetadataStringStub extends Angular2MetadataElementStub<Angular2MetadataString> {


  private final @NotNull StringRef myValue;

  public Angular2MetadataStringStub(@Nullable String memberName,
                                    @NotNull JsonValue source,
                                    @Nullable StubElement parent) {
    super(memberName, parent, Angular2MetadataElementTypes.STRING);
    myValue = StringRef.fromString(((JsonStringLiteral)source).getValue());
  }

  public Angular2MetadataStringStub(@NotNull StubInputStream stream, @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.STRING);
    myValue = Objects.requireNonNull(stream.readName());
  }

  @Override
  public void serialize(@NotNull StubOutputStream stream) throws IOException {
    super.serialize(stream);
    writeString(myValue, stream);
  }

  public @NotNull String getValue() {
    return myValue.getString();
  }
}
