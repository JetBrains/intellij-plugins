// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import one.util.streamex.StreamEx;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataNodeModule;
import org.angular2.index.Angular2MetadataNodeModuleIndex;
import org.angular2.lang.metadata.MetadataUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Angular2MetadataNodeModuleStub extends Angular2MetadataElementStub<Angular2MetadataNodeModule> {

  @NonNls private static final String IMPORT_AS = "importAs";
  @NonNls private static final String EXPORTS = "exports";
  private static final String METADATA = "metadata";

  private final @Nullable StringRef myImportAs;

  public Angular2MetadataNodeModuleStub(@NotNull StubInputStream stream, @Nullable StubElement parentStub) throws IOException {
    super(stream, parentStub, Angular2MetadataElementTypes.NODE_MODULE);
    myImportAs = stream.readName();
  }

  public Angular2MetadataNodeModuleStub(@Nullable StubElement parentStub, @Nullable JsonValue fileRoot) {
    super((String)null, parentStub, Angular2MetadataElementTypes.NODE_MODULE);
    if (fileRoot instanceof JsonArray) {
      fileRoot = ((JsonArray)fileRoot).getValueList().get(0);
    }
    if (fileRoot instanceof JsonObject) {
      JsonObject fileRootObject = (JsonObject)fileRoot;
      myImportAs = StringRef.fromString(MetadataUtils.readStringPropertyValue(fileRootObject.findProperty(IMPORT_AS)));
      StreamEx.ofNullable(MetadataUtils.getPropertyValue(fileRootObject.findProperty(EXPORTS), JsonArray.class))
        .flatCollection(JsonArray::getValueList)
        .select(JsonObject.class)
        .forEach(object -> new Angular2MetadataModuleExportStub(this, object));
      MetadataUtils.streamObjectProperty(fileRootObject.findProperty(METADATA))
        .forEach(this::loadMemberProperty);
    }
    else {
      myImportAs = null;
    }
  }

  @Override
  public void index(@NotNull IndexSink sink) {
    super.index(sink);
    if (getImportAs() != null) {
      sink.occurrence(Angular2MetadataNodeModuleIndex.KEY, getImportAs());
    }
  }

  @Override
  public void serialize(@NotNull StubOutputStream stream) throws IOException {
    super.serialize(stream);
    writeString(myImportAs, stream);
  }

  public @Nullable String getImportAs() {
    return StringRef.toString(myImportAs);
  }
}
