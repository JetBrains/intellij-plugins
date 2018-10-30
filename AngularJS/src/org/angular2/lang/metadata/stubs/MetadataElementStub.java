// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata.stubs;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.lang.javascript.index.flags.FlagsStructure;
import com.intellij.lang.javascript.index.flags.FlagsStructureElement;
import com.intellij.lang.javascript.index.flags.IntFlagsSerializer;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.stubs.*;
import com.intellij.util.io.DataInputOutputUtil;
import com.intellij.util.io.StringRef;
import org.angular2.lang.metadata.psi.MetadataElement;
import org.angular2.lang.metadata.psi.MetadataElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue;

public abstract class MetadataElementStub<Psi extends MetadataElement> extends StubBase<Psi> {

  protected static final String SYMBOL_TYPE = "__symbolic";
  protected static final String SYMBOL_REFERENCE = "reference";
  protected static final String SYMBOL_PROPERTY = "property";

  protected static final String DECORATORS = "decorators";
  protected static final String CALL = "call";
  protected static final String EXPRESSION = "expression";
  protected static final String ARGUMENTS = "arguments";
  protected static final String MEMBERS = "members";
  protected static final String EXTENDS = "extends";
  protected static final String CONSTRUCTOR = "__ctor__";

  protected static final String REFERENCE_NAME = "name";
  protected static final String REFERENCE_MODULE = "module";

  protected static final String ARRAY_TYPE = "#array";
  protected static final String OBJECT_TYPE = "#object";

  protected static final FlagsStructure FLAGS_STRUCTURE = FlagsStructure.EMPTY;

  private final StringRef myMemberName;
  private int myFlags;
  private final AtomicNotNullLazyValue<Map<String, MetadataElementStub>> membersMap =
    new AtomicNotNullLazyValue<Map<String, MetadataElementStub>>() {
      @NotNull
      @Override
      protected Map<String, MetadataElementStub> compute() {
        return getChildrenStubs().stream()
          .filter(stub -> ((MetadataElementStub)stub).getMemberName() != null)
          .collect(Collectors.toMap(stub -> ((MetadataElementStub)stub).getMemberName(),
                                    stub -> (MetadataElementStub)stub,
                                    (a, b) -> a));
      }
    };

  public MetadataElementStub(@Nullable String memberName, @Nullable StubElement parent, @NotNull MetadataElementType elementType) {
    super(parent, elementType);
    myMemberName = StringRef.fromString(memberName);
  }

  public MetadataElementStub(@NotNull StubInputStream stream, @Nullable StubElement parent, @NotNull MetadataElementType elementType)
    throws IOException {
    super(parent, elementType);
    final int flagsSize = getFlagsStructure().size();
    if (flagsSize > 0) {
      assert flagsSize <= Integer.SIZE : this.getClass();
      myFlags = DataInputOutputUtil.readINT(stream);
    }
    myMemberName = stream.readName();
  }

  @Nullable
  public String getMemberName() {
    return StringRef.toString(myMemberName);
  }

  protected abstract Map<String, ConstructorFromJsonValue> getTypeFactory();

  public void serialize(@NotNull StubOutputStream stream) throws IOException {
    if (getFlagsStructure().size() > 0) {
      DataInputOutputUtil.writeINT(stream, myFlags);
    }
    writeString(myMemberName, stream);
  }

  public void index(@NotNull IndexSink sink) {
  }

  protected <T> T readFlag(FlagsStructureElement<T> structureElement) {
    return IntFlagsSerializer.INSTANCE.readValue(getFlagsStructure(), structureElement, myFlags);
  }

  protected <T> void writeFlag(FlagsStructureElement<T> structureElement, T value) {
    // TODO sync
    myFlags = IntFlagsSerializer.INSTANCE.writeValue(getFlagsStructure(), structureElement, value, myFlags);
  }

  protected FlagsStructure getFlagsStructure() {
    return FlagsStructure.EMPTY;
  }

  protected void loadMemberProperty(JsonProperty p) {
    createMember(p.getName(), p.getValue());
  }

  protected void createMember(@Nullable String name, @Nullable JsonValue member) {
    ConstructorFromJsonValue constructor = null;
    if (member instanceof JsonArray) {
      constructor = getTypeFactory().get(ARRAY_TYPE);
    }
    else if (member instanceof JsonObject) {
      String type = readStringPropertyValue(((JsonObject)member).findProperty(SYMBOL_TYPE));
      constructor = getTypeFactory().get(type == null ? OBJECT_TYPE : type);
    }
    if (constructor != null) {
      constructor.construct(name, member, this);
    }
  }

  public MetadataElementStub findMember(String name) {
    return membersMap.getValue().get(name);
  }

  protected static void writeString(@Nullable StringRef ref, @NotNull final StubOutputStream dataStream) throws IOException {
    dataStream.writeName(StringRef.toString(ref));
  }

  protected static void writeStringMap(@NotNull Map<String, String> map, @NotNull StubOutputStream stream) throws IOException {
    stream.writeVarInt(map.size());
    for (Map.Entry<String, String> e : map.entrySet()) {
      stream.writeName(e.getKey());
      stream.writeName(e.getValue());
    }
  }

  @NotNull
  protected static Map<String, String> readStringMap(@NotNull StubInputStream stream) throws IOException {
    Map<String, String> result = new HashMap<>();
    int size = stream.readVarInt();
    for (int i = 0; i < size; i++) {
      String key = stream.readNameString();
      String value = stream.readNameString();
      result.put(key, value);
    }
    return result;
  }

  @NotNull
  protected static Stream<Pair<String, JsonObject>> streamDecorators(@NotNull JsonObject sourceClass) {
    JsonArray list = tryCast(doIfNotNull(sourceClass.findProperty(DECORATORS), JsonProperty::getValue), JsonArray.class);
    if (list == null) {
      return Stream.empty();
    }
    return list.getValueList().stream()
      .map(v -> tryCast(v, JsonObject.class))
      .filter(obj -> obj != null
                     && CALL.equals(readStringPropertyValue(obj.findProperty(SYMBOL_TYPE))))
      .map(obj -> tryCast(doIfNotNull(obj.findProperty(EXPRESSION), JsonProperty::getValue), JsonObject.class))
      .filter(obj -> obj != null
                     && SYMBOL_REFERENCE.equals(readStringPropertyValue(obj.findProperty(SYMBOL_TYPE))))
      .map(obj -> Pair.create(readStringPropertyValue(obj.findProperty(REFERENCE_NAME)), (JsonObject)obj.getParent().getParent()))
      .filter(pair -> pair.first != null);
  }

  @Nullable
  protected static <T extends JsonValue> T getDecoratorInitializer(@NotNull JsonObject decorator, Class<T> initializerClass) {
    JsonArray args = tryCast(doIfNotNull(decorator.findProperty(ARGUMENTS), JsonProperty::getValue), JsonArray.class);
    return args != null && args.getValueList().size() == 1 ? tryCast(args.getValueList().get(0), initializerClass) : null;
  }

  protected interface ConstructorFromJsonValue {
    MetadataElementStub construct(@Nullable String memberName,
                                  @NotNull JsonValue source,
                                  @Nullable StubElement parent);
  }
}
