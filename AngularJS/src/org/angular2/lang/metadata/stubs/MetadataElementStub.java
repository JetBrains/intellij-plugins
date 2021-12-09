// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata.stubs;

import com.intellij.json.psi.*;
import com.intellij.lang.javascript.index.flags.BooleanStructureElement;
import com.intellij.lang.javascript.index.flags.FlagsStructure;
import com.intellij.lang.javascript.index.flags.FlagsStructureElement;
import com.intellij.lang.javascript.index.flags.IntFlagsSerializer;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.DataInputOutputUtilRt;
import com.intellij.psi.stubs.*;
import com.intellij.util.io.DataInputOutputUtil;
import com.intellij.util.io.StringRef;
import org.angular2.lang.metadata.psi.MetadataElement;
import org.angular2.lang.metadata.psi.MetadataElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue;

public abstract class MetadataElementStub<Psi extends MetadataElement> extends StubBase<Psi> {
  @NonNls protected static final String SYMBOL_TYPE = "__symbolic";
  @NonNls protected static final String SYMBOL_REFERENCE = "reference";
  @NonNls protected static final String SYMBOL_PROPERTY = "property";
  @NonNls protected static final String SYMBOL_FUNCTION = "function";
  @NonNls protected static final String SYMBOL_METHOD = "method";
  @NonNls protected static final String SYMBOL_CALL = "call";
  @NonNls protected static final String SYMBOL_CLASS = "class";
  @NonNls protected static final String SYMBOL_SPREAD = "spread";

  @NonNls protected static final String PARAMETER_DECORATORS = "parameterDecorators";
  @NonNls protected static final String DECORATORS = "decorators";
  @NonNls protected static final String EXPRESSION = "expression";
  @NonNls protected static final String ARGUMENTS = "arguments";
  @NonNls protected static final String MEMBERS = "members";
  @NonNls protected static final String STATICS = "statics";
  @NonNls protected static final String EXTENDS = "extends";
  @NonNls protected static final String CONSTRUCTOR = "__ctor__";

  @NonNls protected static final String REFERENCE_NAME = "name";
  @NonNls protected static final String REFERENCE_MODULE = "module";

  @NonNls protected static final String FUNCTION_VALUE = "value";

  @NonNls protected static final String STRING_TYPE = "#string";
  @NonNls protected static final String ARRAY_TYPE = "#array";
  @NonNls protected static final String OBJECT_TYPE = "#object";

  private static final BooleanStructureElement HAS_MEMBER_NAME = new BooleanStructureElement();
  protected static final FlagsStructure FLAGS_STRUCTURE = new FlagsStructure(
    HAS_MEMBER_NAME
  );

  private final StringRef myMemberName;
  private int myFlags;
  private final NotNullLazyValue<Map<String, MetadataElementStub>> membersMap = NotNullLazyValue.lazy(() -> {
    return getChildrenStubs().stream()
      .filter(stub -> ((MetadataElementStub<?>)stub).getMemberName() != null)
      .collect(Collectors.toMap(stub -> ((MetadataElementStub<?>)stub).getMemberName(),
                                stub -> (MetadataElementStub)stub,
                                (a, b) -> a));
  });

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
    myMemberName = readFlag(HAS_MEMBER_NAME) ? stream.readName() : null;
  }

  public @Nullable String getMemberName() {
    return StringRef.toString(myMemberName);
  }

  protected abstract Map<String, ConstructorFromJsonValue> getTypeFactory();

  public void serialize(@NotNull StubOutputStream stream) throws IOException {
    writeFlag(HAS_MEMBER_NAME, myMemberName != null);
    if (getFlagsStructure().size() > 0) {
      DataInputOutputUtil.writeINT(stream, myFlags);
    }
    if (myMemberName != null) {
      writeString(myMemberName, stream);
    }
  }

  public void index(@NotNull IndexSink sink) {
  }

  protected <T> T readFlag(FlagsStructureElement<T> structureElement) {
    return IntFlagsSerializer.INSTANCE.readValue(getFlagsStructure(), structureElement, myFlags);
  }

  protected <T> void writeFlag(FlagsStructureElement<T> structureElement, T value) {
    myFlags = IntFlagsSerializer.INSTANCE.writeValue(getFlagsStructure(), structureElement, value, myFlags);
  }

  protected FlagsStructure getFlagsStructure() {
    return FLAGS_STRUCTURE;
  }

  protected void loadMemberProperty(@NotNull JsonProperty p) {
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
    else if (member instanceof JsonStringLiteral) {
      constructor = getTypeFactory().get(STRING_TYPE);
    }
    if (constructor != null) {
      constructor.construct(name, member, this);
    }
  }

  public MetadataElementStub findMember(@Nullable String name) {
    return membersMap.getValue().get(name);
  }

  protected static void writeString(@Nullable StringRef ref, final @NotNull StubOutputStream dataStream) throws IOException {
    dataStream.writeName(StringRef.toString(ref));
  }

  protected static void writeStringMap(@NotNull Map<String, String> map, @NotNull StubOutputStream stream) throws IOException {
    DataInputOutputUtilRt.writeMap(stream, map, stream::writeName, stream::writeName);
  }

  protected static void writeIntegerMap(final @NotNull Map<String, Integer> map, final @NotNull StubOutputStream stream)
    throws IOException {
    DataInputOutputUtilRt.writeMap(stream, map, stream::writeName, stream::writeVarInt);
  }

  protected static @NotNull Map<String, String> readStringMap(@NotNull StubInputStream stream) throws IOException {
    return DataInputOutputUtilRt.readMap(stream, stream::readNameString, stream::readNameString);
  }

  protected static void writeStringList(@NotNull List<String> list, @NotNull StubOutputStream stream) throws IOException {
    DataInputOutputUtilRt.writeSeq(stream, list, stream::writeName);
  }

  protected static @NotNull List<String> readStringList(@NotNull StubInputStream stream) throws IOException {
    return DataInputOutputUtilRt.readSeq(stream, stream::readNameString);
  }

  protected static @NotNull Map<String, Integer> readIntegerMap(final @NotNull StubInputStream stream) throws IOException {
    return DataInputOutputUtilRt.readMap(stream, stream::readNameString, stream::readVarInt);
  }

  protected static @NotNull Stream<Pair<String, JsonObject>> streamDecorators(@NotNull JsonObject sourceClass) {
    JsonArray list = tryCast(doIfNotNull(sourceClass.findProperty(DECORATORS), JsonProperty::getValue), JsonArray.class);
    if (list == null) {
      return Stream.empty();
    }
    return list.getValueList().stream()
      .map(v -> tryCast(v, JsonObject.class))
      .filter(obj -> obj != null
                     && SYMBOL_CALL.equals(readStringPropertyValue(obj.findProperty(SYMBOL_TYPE))))
      .map(obj -> tryCast(doIfNotNull(obj.findProperty(EXPRESSION), JsonProperty::getValue), JsonObject.class))
      .filter(obj -> obj != null
                     && SYMBOL_REFERENCE.equals(readStringPropertyValue(obj.findProperty(SYMBOL_TYPE))))
      .map(obj -> Pair.create(readStringPropertyValue(obj.findProperty(REFERENCE_NAME)), (JsonObject)obj.getParent().getParent()))
      .filter(pair -> pair.first != null);
  }

  protected static @Nullable <T extends JsonValue> T getDecoratorInitializer(@NotNull JsonObject decorator, Class<T> initializerClass) {
    JsonArray args = tryCast(doIfNotNull(decorator.findProperty(ARGUMENTS), JsonProperty::getValue), JsonArray.class);
    return args != null && args.getValueList().size() == 1 ? tryCast(args.getValueList().get(0), initializerClass) : null;
  }

  protected interface ConstructorFromJsonValue {
    MetadataElementStub construct(@Nullable String memberName,
                                  @NotNull JsonValue source,
                                  @Nullable StubElement parent);
  }
}
