// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.*;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.entities.metadata.psi.Angular2MetadataDirectiveBase;
import org.angular2.index.Angular2MetadataDirectiveIndex;
import org.angular2.lang.metadata.psi.MetadataElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.Angular2DecoratorUtil.EXPORT_AS_PROP;
import static org.angular2.Angular2DecoratorUtil.SELECTOR_PROP;
import static org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue;

public abstract class Angular2MetadataDirectiveStubBase<Psi extends Angular2MetadataDirectiveBase> extends Angular2MetadataEntityStub<Psi> {

  private StringRef mySelector;
  private StringRef myExportAs;

  public Angular2MetadataDirectiveStubBase(@Nullable String memberName,
                                           @Nullable StubElement parent,
                                           @NotNull JsonObject source,
                                           @NotNull JsonObject initializer,
                                           @NotNull MetadataElementType elementType) {
    super(memberName, parent, source, elementType);
    loadInitializer(initializer);
  }

  public Angular2MetadataDirectiveStubBase(@NotNull StubInputStream stream,
                                           @Nullable StubElement parent, @NotNull MetadataElementType elementType)
    throws IOException {
    super(stream, parent, elementType);
  }

  @NotNull
  public String getSelector() {
    return StringRef.toString(mySelector);
  }

  @Nullable
  public String getExportAs() {
    return StringRef.toString(myExportAs);
  }

  @Override
  public void serialize(@NotNull StubOutputStream stream) throws IOException {
    super.serialize(stream);
    writeString(mySelector, stream);
    writeString(myExportAs, stream);
  }

  @Override
  public void deserialize(@NotNull StubInputStream stream) throws IOException {
    super.deserialize(stream);
    mySelector = stream.readName();
    myExportAs = stream.readName();
  }

  private void loadInitializer(@NotNull JsonObject initializer) {
    mySelector = StringRef.fromString(readStringPropertyValue(initializer.findProperty(SELECTOR_PROP)));
    assert mySelector != null;
    myExportAs = StringRef.fromString(readStringPropertyValue(initializer.findProperty(EXPORT_AS_PROP)));
    loadAdditionalBindingMappings(myInputMappings, initializer, Angular2DecoratorUtil.INPUTS_PROP);
    loadAdditionalBindingMappings(myOutputMappings, initializer, Angular2DecoratorUtil.OUTPUTS_PROP);
  }

  protected abstract boolean isTemplate();

  @Override
  public void index(@NotNull IndexSink sink) {
    super.index(sink);
    Angular2EntityUtils.getDirectiveIndexNames(getSelector(), isTemplate())
      .forEach(indexName -> sink.occurrence(Angular2MetadataDirectiveIndex.KEY, indexName));
  }

  private static void loadAdditionalBindingMappings(@NotNull Map<String, String> mappings,
                                                    @NotNull JsonObject initializer,
                                                    @NotNull String propertyName) {
    JsonArray list = tryCast(doIfNotNull(initializer.findProperty(propertyName), JsonProperty::getValue), JsonArray.class);
    if (list != null) {
      for (JsonValue v : list.getValueList()) {
        if (v instanceof JsonStringLiteral) {
          String value = ((JsonStringLiteral)v).getValue();
          Pair<String, String> p = Angular2EntityUtils.parsePropertyMapping(value);
          mappings.putIfAbsent(p.first, p.second);
        }
      }
    }
  }
}
