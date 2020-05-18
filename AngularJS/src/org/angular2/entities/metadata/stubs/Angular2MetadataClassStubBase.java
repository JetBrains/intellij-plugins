// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.*;
import com.intellij.lang.javascript.index.flags.BooleanStructureElement;
import com.intellij.lang.javascript.index.flags.FlagsStructure;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2DirectiveKind;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.entities.metadata.psi.Angular2MetadataClassBase;
import org.angular2.index.Angular2MetadataClassNameIndex;
import org.angular2.lang.metadata.MetadataUtils;
import org.angular2.lang.metadata.psi.MetadataElementType;
import org.angular2.lang.metadata.stubs.MetadataElementStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.lang.metadata.MetadataUtils.getPropertyValue;
import static org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue;

public class Angular2MetadataClassStubBase<Psi extends Angular2MetadataClassBase> extends Angular2MetadataElementStub<Psi> {

  @NonNls private static final String EXTENDS_MEMBER = "#ext";

  private static final AtomicNotNullLazyValue<Map<String, EntityFactory>> ENTITY_FACTORIES =
    new AtomicNotNullLazyValue<Map<String, EntityFactory>>() {
      @Override
      protected @NotNull Map<String, EntityFactory> compute() {
        return ContainerUtil.<String, EntityFactory>immutableMapBuilder()
          .put(MODULE_DEC, Angular2MetadataModuleStub::new)
          .put(PIPE_DEC, Angular2MetadataPipeStub::createPipeStub)
          .put(COMPONENT_DEC, Angular2MetadataComponentStub::new)
          .put(DIRECTIVE_DEC, Angular2MetadataDirectiveStub::new)
          .build();
      }
    };

  private static Map<String, EntityFactory> getEntityFactories() {
    return ENTITY_FACTORIES.getValue();
  }

  public static Angular2MetadataClassStubBase<?> createClassStub(@Nullable String memberName,
                                                                 @NotNull JsonValue source,
                                                                 @Nullable StubElement parent) {
    return streamDecorators((JsonObject)source)
      .map(pair -> doIfNotNull(getEntityFactories().get(pair.first),
                               factory -> factory.create(memberName, parent, (JsonObject)source, pair.second)))
      .filter(Objects::nonNull)
      .findFirst()
      .orElseGet(() -> new Angular2MetadataClassStub(memberName, source, parent));
  }

  private static final BooleanStructureElement IS_STRUCTURAL_DIRECTIVE_FLAG = new BooleanStructureElement();
  private static final BooleanStructureElement IS_REGULAR_DIRECTIVE_FLAG = new BooleanStructureElement();
  private static final BooleanStructureElement HAS_INPUT_MAPPINGS = new BooleanStructureElement();
  private static final BooleanStructureElement HAS_OUTPUT_MAPPINGS = new BooleanStructureElement();
  @SuppressWarnings("StaticFieldReferencedViaSubclass")
  protected static final FlagsStructure FLAGS_STRUCTURE = new FlagsStructure(
    Angular2MetadataElementStub.FLAGS_STRUCTURE,
    IS_STRUCTURAL_DIRECTIVE_FLAG,
    IS_REGULAR_DIRECTIVE_FLAG,
    HAS_INPUT_MAPPINGS,
    HAS_OUTPUT_MAPPINGS
  );

  protected final Map<String, String> myInputMappings;
  protected final Map<String, String> myOutputMappings;

  public Angular2MetadataClassStubBase(@Nullable String memberName,
                                       @Nullable StubElement parent,
                                       @NotNull JsonObject source,
                                       @NotNull MetadataElementType elementType) {
    super(memberName, parent, elementType);
    if (loadInOuts()) {
      readTemplateFlag(source);
    }
    JsonObject extendsClass = getPropertyValue(source.findProperty(EXTENDS), JsonObject.class);
    if (extendsClass != null) {
      Angular2MetadataReferenceStub.createReferenceStub(EXTENDS_MEMBER, extendsClass, this);
    }
    myOutputMappings = new HashMap<>();
    myInputMappings = new HashMap<>();
    MetadataUtils.streamObjectProperty(source.findProperty(MEMBERS))
      .forEach(this::loadMember);
    MetadataUtils.streamObjectProperty(source.findProperty(STATICS))
      .filter(prop -> prop.getValue() instanceof JsonObject
                      && SYMBOL_FUNCTION.equals(readStringPropertyValue(((JsonObject)prop.getValue()).findProperty(SYMBOL_TYPE))))
      .forEach(this::loadMemberProperty);
  }

  public Angular2MetadataClassStubBase(@NotNull StubInputStream stream,
                                       @Nullable StubElement parent, @NotNull MetadataElementType elementType) throws IOException {
    super(stream, parent, elementType);
    myInputMappings = readFlag(HAS_INPUT_MAPPINGS) ? MetadataElementStub.readStringMap(stream) : Collections.emptyMap();
    myOutputMappings = readFlag(HAS_OUTPUT_MAPPINGS) ? MetadataElementStub.readStringMap(stream) : Collections.emptyMap();
  }

  public @Nullable String getClassName() {
    return getMemberName();
  }

  public Angular2MetadataReferenceStub getExtendsReference() {
    return (Angular2MetadataReferenceStub)getChildrenStubs().stream()
      .filter(child -> child instanceof Angular2MetadataReferenceStub
                       && EXTENDS_MEMBER.equals(((Angular2MetadataReferenceStub)child).getMemberName()))
      .findFirst()
      .orElse(null);
  }

  public Map<String, String> getInputMappings() {
    return Collections.unmodifiableMap(myInputMappings);
  }

  public Map<String, String> getOutputMappings() {
    return Collections.unmodifiableMap(myOutputMappings);
  }

  public @Nullable Angular2DirectiveKind getDirectiveKind() {
    boolean isStructural = readFlag(IS_STRUCTURAL_DIRECTIVE_FLAG);
    boolean isRegular = readFlag(IS_REGULAR_DIRECTIVE_FLAG);
    if (isStructural || isRegular) {
      return Angular2DirectiveKind.get(isRegular, isStructural);
    }
    return null;
  }

  @Override
  public void serialize(@NotNull StubOutputStream stream) throws IOException {
    writeFlag(HAS_INPUT_MAPPINGS, !myInputMappings.isEmpty());
    writeFlag(HAS_OUTPUT_MAPPINGS, !myOutputMappings.isEmpty());
    super.serialize(stream);
    if (!myInputMappings.isEmpty()) {
      writeStringMap(myInputMappings, stream);
    }
    if (!myOutputMappings.isEmpty()) {
      writeStringMap(myOutputMappings, stream);
    }
  }

  @Override
  public void index(@NotNull IndexSink sink) {
    super.index(sink);
    if (getClassName() != null) {
      sink.occurrence(Angular2MetadataClassNameIndex.KEY, getClassName());
    }
  }

  protected boolean loadInOuts() {
    return true;
  }

  @Override
  protected FlagsStructure getFlagsStructure() {
    return FLAGS_STRUCTURE;
  }

  private void readTemplateFlag(JsonObject source) {
    JsonObject members = tryCast(doIfNotNull(source.findProperty(MEMBERS), JsonProperty::getValue), JsonObject.class);
    JsonProperty constructor = members != null ? members.findProperty(CONSTRUCTOR) : null;
    String constructorText = constructor != null ? constructor.getText() : "";
    Angular2DirectiveKind kind = Angular2DirectiveKind.get(
      constructorText.contains(Angular2EntityUtils.ELEMENT_REF),
      constructorText.contains(Angular2EntityUtils.TEMPLATE_REF),
      constructorText.contains(Angular2EntityUtils.VIEW_CONTAINER_REF)
    );
    writeFlag(IS_STRUCTURAL_DIRECTIVE_FLAG, kind != null && kind.isStructural());
    writeFlag(IS_REGULAR_DIRECTIVE_FLAG, kind != null && kind.isRegular());
  }

  private void loadMember(@NotNull JsonProperty property) {
    String name = property.getName();
    JsonArray val = tryCast(property.getValue(), JsonArray.class);
    if (val == null || val.getValueList().size() != 1) {
      return;
    }
    JsonObject obj = tryCast(val.getValueList().get(0), JsonObject.class);
    if (obj == null) {
      return;
    }
    String memberSymbol = readStringPropertyValue(obj.findProperty(SYMBOL_TYPE));
    if (loadInOuts() && (SYMBOL_PROPERTY.equals(memberSymbol) || SYMBOL_METHOD.equals(memberSymbol))) {
      streamDecorators(obj).forEach(dec -> {
        if (INPUT_DEC.equals(dec.first)) {
          addBindingMapping(name, myInputMappings, getDecoratorInitializer(dec.second, JsonStringLiteral.class));
        }
        else if (OUTPUT_DEC.equals(dec.first)) {
          addBindingMapping(name, myOutputMappings, getDecoratorInitializer(dec.second, JsonStringLiteral.class));
        }
      });
    }
    else if (SYMBOL_FUNCTION.equals(memberSymbol)) {
      loadMemberProperty(property);
    }
  }

  private static void addBindingMapping(@NotNull String fieldName,
                                        @NotNull Map<String, String> mappings,
                                        @Nullable JsonStringLiteral initializer) {
    String bindingName = initializer != null ? initializer.getValue() : fieldName;
    mappings.put(fieldName, bindingName);
  }

  private interface EntityFactory {
    @Nullable
    Angular2MetadataClassStubBase create(@Nullable String memberName,
                                         @Nullable StubElement parent,
                                         @NotNull JsonObject classSource,
                                         @NotNull JsonObject decoratorSource);
  }
}
