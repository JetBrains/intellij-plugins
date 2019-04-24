// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.*;
import com.intellij.lang.javascript.index.flags.BooleanStructureElement;
import com.intellij.lang.javascript.index.flags.FlagsStructure;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.StringRef;
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
import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue;

public abstract class Angular2MetadataDirectiveStubBase<Psi extends Angular2MetadataDirectiveBase> extends Angular2MetadataEntityStub<Psi> {

  private static final BooleanStructureElement HAS_EXPORT_AS = new BooleanStructureElement();
  @SuppressWarnings("StaticFieldReferencedViaSubclass")
  protected static final FlagsStructure FLAGS_STRUCTURE = new FlagsStructure(
    Angular2MetadataEntityStub.FLAGS_STRUCTURE,
    HAS_EXPORT_AS
  );

  private final StringRef mySelector;
  private final StringRef myExportAs;

  public Angular2MetadataDirectiveStubBase(@Nullable String memberName,
                                           @Nullable StubElement parent,
                                           @NotNull JsonObject source,
                                           @NotNull JsonObject decoratorSource,
                                           @NotNull MetadataElementType elementType) {
    super(memberName, parent, source, elementType);
    JsonObject initializer = getDecoratorInitializer(decoratorSource, JsonObject.class);
    if (initializer == null) {
      mySelector = null;
      myExportAs = null;
      return;
    }
    mySelector = StringRef.fromString(readStringPropertyValue(initializer.findProperty(SELECTOR_PROP)));
    myExportAs = StringRef.fromString(readStringPropertyValue(initializer.findProperty(EXPORT_AS_PROP)));
    loadAdditionalBindingMappings(myInputMappings, initializer, INPUTS_PROP);
    loadAdditionalBindingMappings(myOutputMappings, initializer, OUTPUTS_PROP);
  }

  public Angular2MetadataDirectiveStubBase(@NotNull StubInputStream stream,
                                           @Nullable StubElement parent, @NotNull MetadataElementType elementType)
    throws IOException {
    super(stream, parent, elementType);
    mySelector = stream.readName();
    myExportAs = readFlag(HAS_EXPORT_AS) ? stream.readName() : null;
  }

  @Nullable
  public String getSelector() {
    return StringRef.toString(mySelector);
  }

  @Nullable
  public String getExportAs() {
    return StringRef.toString(myExportAs);
  }

  @Override
  public void serialize(@NotNull StubOutputStream stream) throws IOException {
    writeFlag(HAS_EXPORT_AS, myExportAs != null);
    super.serialize(stream);
    writeString(mySelector, stream);
    if (myExportAs != null) {
      writeString(myExportAs, stream);
    }
  }

  @Override
  public void index(@NotNull IndexSink sink) {
    super.index(sink);
    if (getSelector() != null) {
      Angular2EntityUtils.getDirectiveIndexNames(getSelector())
        .forEach(indexName -> sink.occurrence(Angular2MetadataDirectiveIndex.KEY, indexName));
    }
  }

  @Override
  protected FlagsStructure getFlagsStructure() {
    return FLAGS_STRUCTURE;
  }

  private void loadAdditionalBindingMappings(@NotNull Map<String, String> mappings,
                                             @NotNull JsonObject initializer,
                                             @NotNull String propertyName) {
    JsonArray list = tryCast(doIfNotNull(initializer.findProperty(propertyName), JsonProperty::getValue), JsonArray.class);
    if (list != null && ContainerUtil.all(list.getValueList(), JsonStringLiteral.class::isInstance)) {
      for (JsonValue v : list.getValueList()) {
        if (v instanceof JsonStringLiteral) {
          String value = ((JsonStringLiteral)v).getValue();
          Pair<String, String> p = Angular2EntityUtils.parsePropertyMapping(value);
          mappings.putIfAbsent(p.first, p.second);
        }
      }
    }
    else {
      stubDecoratorFields(initializer, propertyName);
    }
  }
}
