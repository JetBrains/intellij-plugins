// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataComponent;
import org.angular2.lang.metadata.MetadataUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static org.angular2.Angular2DecoratorUtil.SELECTOR_PROP;

public class Angular2MetadataComponentStub extends Angular2MetadataDirectiveStubBase<Angular2MetadataComponent> {

  @Nullable
  public static Angular2MetadataComponentStub createComponentStub(@Nullable String memberName,
                                                                  @Nullable StubElement parent,
                                                                  @NotNull JsonObject classSource,
                                                                  @NotNull JsonObject decoratorSource) {
    JsonObject decoratorArg = getDecoratorInitializer(decoratorSource, JsonObject.class);
    if (decoratorArg != null) {
      String selector = MetadataUtils.readStringPropertyValue(decoratorArg.findProperty(SELECTOR_PROP));
      if (StringUtil.isNotEmpty(selector)) {
        return new Angular2MetadataComponentStub(memberName, parent, classSource, decoratorArg);
      }
    }
    return null;
  }

  public Angular2MetadataComponentStub(@Nullable String memberName,
                                       @Nullable StubElement parent,
                                       @NotNull JsonObject source,
                                       @NotNull JsonObject initializer) {
    super(memberName, parent, source, initializer, Angular2MetadataElementTypes.COMPONENT);
  }

  public Angular2MetadataComponentStub(@NotNull StubInputStream stream,
                                       @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.COMPONENT);
  }

  @Override
  protected boolean isTemplate() {
    return false;
  }
}