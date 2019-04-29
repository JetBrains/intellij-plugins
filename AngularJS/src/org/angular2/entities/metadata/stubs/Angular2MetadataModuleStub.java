// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataModule;
import org.angular2.index.Angular2IndexingHandler;
import org.angular2.index.Angular2MetadataModuleIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static org.angular2.Angular2DecoratorUtil.*;

public class Angular2MetadataModuleStub extends Angular2MetadataEntityStub<Angular2MetadataModule> {

  private static final String[] STUBBED_DECORATOR_FIELDS = new String[]{DECLARATIONS_PROP, EXPORTS_PROP, IMPORTS_PROP};

  public Angular2MetadataModuleStub(@Nullable String memberName,
                                    @Nullable StubElement parent,
                                    @NotNull JsonObject classSource,
                                    @NotNull JsonObject decoratorSource) {
    super(memberName, parent, classSource, Angular2MetadataElementTypes.MODULE);

    JsonObject initializer = getDecoratorInitializer(decoratorSource, JsonObject.class);
    if (initializer != null) {
      stubDecoratorFields(initializer, STUBBED_DECORATOR_FIELDS);
    }
  }

  public Angular2MetadataModuleStub(@NotNull StubInputStream stream,
                                    @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.MODULE);
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
