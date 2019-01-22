// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataModule;
import org.angular2.index.Angular2IndexingHandler;
import org.angular2.index.Angular2MetadataModuleIndex;
import org.angular2.lang.metadata.stubs.MetadataElementStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static org.angular2.Angular2DecoratorUtil.*;

public class Angular2MetadataModuleStub extends Angular2MetadataEntityStub<Angular2MetadataModule> {

  private static final String[] INTERESTED_IN = new String[]{DECLARATIONS_PROP, EXPORTS_PROP, IMPORTS_PROP};

  private static final String DECORATOR_FIELD_PREFIX = "___dec.";

  @SuppressWarnings("unused")
  @Nullable
  static Angular2MetadataClassStubBase createModuleStub(@Nullable String memberName,
                                                        @Nullable StubElement parent,
                                                        @NotNull JsonObject classSource,
                                                        @NotNull JsonObject decoratorSource) {
    JsonObject decoratorArg = getDecoratorInitializer(decoratorSource, JsonObject.class);
    if (decoratorArg != null) {
      return new Angular2MetadataModuleStub(memberName, parent, classSource, decoratorArg);
    }
    return null;
  }

  public Angular2MetadataModuleStub(@Nullable String memberName,
                                    @Nullable StubElement parent,
                                    @NotNull JsonObject classSource,
                                    @NotNull JsonObject initializer) {
    super(memberName, parent, classSource, Angular2MetadataElementTypes.MODULE);
    for (String name : INTERESTED_IN) {
      JsonProperty property = initializer.findProperty(name);
      if (property != null && property.getValue() instanceof JsonArray) {
        new Angular2MetadataArrayStub(DECORATOR_FIELD_PREFIX + name,
                                      property.getValue(), this);
      }
    }
  }

  public Angular2MetadataModuleStub(@NotNull StubInputStream stream,
                                    @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.MODULE);
  }

  public StubElement getModuleConfigPropertyValueStub(@NotNull String name) {
    name = DECORATOR_FIELD_PREFIX + name;
    for (StubElement stub : getChildrenStubs()) {
      if (name.equals(((MetadataElementStub)stub).getMemberName())) {
        return stub;
      }
    }
    return null;
  }

  @Override
  public void index(@NotNull IndexSink sink) {
    super.index(sink);
    sink.occurrence(Angular2MetadataModuleIndex.KEY,
                    Angular2IndexingHandler.NG_MODULE_INDEX_NAME);
  }

  @Override
  protected boolean loadInOuts() {
    return false;
  }
}
