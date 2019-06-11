// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataElement;
import org.angular2.entities.metadata.psi.Angular2MetadataSpread;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue;

public class Angular2MetadataSpreadStub extends Angular2MetadataElementStub<Angular2MetadataSpread> {

  @NonNls private static final String SPREAD_EXPRESSION = "#expression";

  public static Angular2MetadataSpreadStub createSpreadStub(@Nullable String memberName,
                                                            @NotNull JsonValue source,
                                                            @Nullable StubElement parent) {
    JsonObject sourceObject = (JsonObject)source;
    if (SYMBOL_SPREAD.equals(readStringPropertyValue(sourceObject.findProperty(SYMBOL_TYPE)))) {
      JsonValue spreadExpression = doIfNotNull(sourceObject.findProperty(EXPRESSION), JsonProperty::getValue);
      if (spreadExpression != null) {
        return new Angular2MetadataSpreadStub(memberName, spreadExpression, parent);
      }
    }
    return null;
  }

  private Angular2MetadataSpreadStub(@Nullable String memberName,
                                     @NotNull JsonValue spreadExpression,
                                     @Nullable StubElement parent) {
    super(memberName, parent, Angular2MetadataElementTypes.SPREAD);
    createMember(SPREAD_EXPRESSION, spreadExpression);
  }

  public Angular2MetadataSpreadStub(@NotNull StubInputStream stream, @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.SPREAD);
  }

  public Angular2MetadataElementStub<? extends Angular2MetadataElement> getSpreadExpression() {
    //noinspection unchecked
    return tryCast(findMember(SPREAD_EXPRESSION), Angular2MetadataElementStub.class);
  }
}
