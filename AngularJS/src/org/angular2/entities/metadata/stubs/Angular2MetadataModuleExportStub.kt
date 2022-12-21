// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.javascript.index.flags.BooleanStructureElement;
import com.intellij.lang.javascript.index.flags.FlagsStructure;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;
import one.util.streamex.StreamEx;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataModuleExport;
import org.angular2.lang.metadata.MetadataUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class Angular2MetadataModuleExportStub extends Angular2MetadataElementStub<Angular2MetadataModuleExport> {

  @NonNls private static final String FROM = "from";
  @NonNls private static final String EXPORT = "export";

  @NonNls private static final String AS = "as";
  @NonNls private static final String NAME = "name";

  private static final BooleanStructureElement HAS_EXPORT_MAPPINGS = new BooleanStructureElement();
  @SuppressWarnings("StaticFieldReferencedViaSubclass")
  protected static final FlagsStructure FLAGS_STRUCTURE = new FlagsStructure(
    Angular2MetadataElementStub.FLAGS_STRUCTURE,
    HAS_EXPORT_MAPPINGS
  );

  private final StringRef myFrom;
  private final Map<String, String> myExportMappings;

  public Angular2MetadataModuleExportStub(@NotNull StubElement parent,
                                          @NotNull JsonObject source) {
    super((String)null, parent, Angular2MetadataElementTypes.MODULE_EXPORT);
    myFrom = StringRef.fromString(MetadataUtils.readStringPropertyValue(source.findProperty(FROM)));
    myExportMappings = StreamEx.ofNullable(source.findProperty(EXPORT))
      .map(JsonProperty::getValue)
      .select(JsonArray.class)
      .flatCollection(JsonArray::getValueList)
      .select(JsonObject.class)
      .map(obj -> {
        String name = MetadataUtils.readStringPropertyValue(obj.findProperty(NAME));
        String as = MetadataUtils.readStringPropertyValue(obj.findProperty(AS));
        return name == null || as == null
               ? null
               : Pair.pair(as, name);
      })
      .nonNull()
      .mapToEntry(p -> p.first, p -> p.second)
      .distinct()
      .toImmutableMap();
  }

  public Angular2MetadataModuleExportStub(@NotNull StubInputStream stream,
                                          @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.MODULE_EXPORT);
    myFrom = stream.readName();
    myExportMappings = readFlag(HAS_EXPORT_MAPPINGS) ? Collections.unmodifiableMap(readStringMap(stream)) : Collections.emptyMap();
  }


  @Override
  public void serialize(@NotNull StubOutputStream stream) throws IOException {
    writeFlag(HAS_EXPORT_MAPPINGS, !myExportMappings.isEmpty());
    super.serialize(stream);
    writeString(myFrom, stream);
    if (!myExportMappings.isEmpty()) {
      writeStringMap(myExportMappings, stream);
    }
  }

  public @Nullable String getFrom() {
    return StringRef.toString(myFrom);
  }

  public @NotNull Map<String, String> getExportMappings() {
    return myExportMappings;
  }

  @Override
  protected FlagsStructure getFlagsStructure() {
    return FLAGS_STRUCTURE;
  }
}
