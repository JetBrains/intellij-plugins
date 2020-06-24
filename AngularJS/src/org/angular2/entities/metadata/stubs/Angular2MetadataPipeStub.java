// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataPipe;
import org.angular2.index.Angular2MetadataPipeIndex;
import org.angular2.lang.metadata.MetadataUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Angular2MetadataPipeStub extends Angular2MetadataEntityStub<Angular2MetadataPipe> {

  public static @Nullable Angular2MetadataPipeStub createPipeStub(@Nullable String memberName,
                                                                  @Nullable StubElement parent,
                                                                  @NotNull JsonObject classSource,
                                                                  @NotNull JsonObject decoratorSource) {
    JsonObject decoratorArg = getDecoratorInitializer(decoratorSource, JsonObject.class);
    if (decoratorArg != null) {
      String pipeName = MetadataUtils.readStringPropertyValue(decoratorArg.findProperty(NAME));
      if (pipeName != null) {
        return new Angular2MetadataPipeStub(memberName, parent, classSource, pipeName);
      }
    }
    return null;
  }

  private final StringRef myPipeName;

  private Angular2MetadataPipeStub(@Nullable String memberName,
                                   @Nullable StubElement parent,
                                   @NotNull JsonObject classSource,
                                   @NotNull String pipeName) {
    super(memberName, parent, classSource, Angular2MetadataElementTypes.PIPE);
    myPipeName = StringRef.fromString(pipeName);
  }

  public Angular2MetadataPipeStub(@NotNull StubInputStream stream, @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.PIPE);
    myPipeName = stream.readName();
  }

  public @NotNull String getPipeName() {
    return StringRef.toString(myPipeName);
  }

  @Override
  public void serialize(@NotNull StubOutputStream stream) throws IOException {
    super.serialize(stream);
    writeString(myPipeName, stream);
  }

  @Override
  public void index(@NotNull IndexSink sink) {
    super.index(sink);
    sink.occurrence(Angular2MetadataPipeIndex.KEY, getPipeName());
  }

  @Override
  protected boolean loadInOuts() {
    return false;
  }
}
