// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonValue;
import com.intellij.lang.javascript.index.flags.BooleanStructureElement;
import com.intellij.lang.javascript.index.flags.FlagsStructure;
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

  private static final BooleanStructureElement HAS_MODULE_NAME = new BooleanStructureElement();
  @SuppressWarnings("StaticFieldReferencedViaSubclass")
  protected static final FlagsStructure FLAGS_STRUCTURE = new FlagsStructure(
    Angular2MetadataElementStub.FLAGS_STRUCTURE,
    HAS_MODULE_NAME
  );

  private final StringRef myName;
  private final StringRef myModule;

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
    myName = stream.readName();
    myModule = readFlag(HAS_MODULE_NAME) ? stream.readName() : null;
  }

  @Override
  public void serialize(@NotNull StubOutputStream stream) throws IOException {
    writeFlag(HAS_MODULE_NAME, myModule != null);
    super.serialize(stream);
    writeString(myName, stream);
    if (myModule != null) {
      writeString(myModule, stream);
    }
  }

  public @NotNull String getName() {
    return StringRef.toString(myName);
  }

  public @Nullable String getModule() {
    return StringRef.toString(myModule);
  }

  @Override
  public FlagsStructure getFlagsStructure() {
    return FLAGS_STRUCTURE;
  }
}
