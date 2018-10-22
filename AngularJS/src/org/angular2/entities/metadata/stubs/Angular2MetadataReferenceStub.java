// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue;

public class Angular2MetadataReferenceStub extends Angular2MetadataElementStub<Angular2MetadataReference> {

  public static Angular2MetadataReferenceStub createReferenceStub(@Nullable String memberName,
                                                                  @NotNull JsonValue source,
                                                                  @Nullable StubElement parent) {
    JsonObject sourceObject = (JsonObject)source;
    if (SYMBOL_REFERENCE.equals(readStringPropertyValue(sourceObject.findProperty(SYMBOL_TYPE)))) {
      String name = readStringPropertyValue(sourceObject.findProperty(REFERENCE_NAME));
      String module = readStringPropertyValue(sourceObject.findProperty(REFERENCE_MODULE));
      if (name != null) {
        return new Angular2MetadataReferenceStub(memberName, name, module, parent);
      }
    }
    return null;
  }

  private StringRef myName;
  private StringRef myModule;

  private Angular2MetadataReferenceStub(@Nullable String memberName,
                                        @NotNull String name,
                                        @Nullable String module,
                                        @Nullable StubElement parent) {
    super(memberName, parent, Angular2MetadataElementTypes.REFERENCE);
    myName = StringRef.fromString(name);
    myModule = StringRef.fromString(module);
  }

  public Angular2MetadataReferenceStub(@NotNull StubInputStream stream, @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.REFERENCE);
  }

  @Override
  public void serialize(@NotNull StubOutputStream stream) throws IOException {
    super.serialize(stream);
    writeString(myName, stream);
    writeString(myModule, stream);
  }

  @Override
  public void deserialize(@NotNull StubInputStream stream) throws IOException {
    super.deserialize(stream);
    myName = stream.readName();
    myModule = stream.readName();
  }

  public String getName() {
    return StringRef.toString(myName);
  }

  public String getModule() {
    return StringRef.toString(myModule);
  }
}
