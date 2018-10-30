// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.*;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.metadata.psi.Angular2MetadataClassBase;
import org.angular2.lang.metadata.MetadataUtils;
import org.angular2.lang.metadata.psi.MetadataElementType;
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

  private static final String EXTENDS_MEMBER = "#ext";

  private static final AtomicNotNullLazyValue<Map<String, EntityFactory>> ENTITY_FACTORIES =
    new AtomicNotNullLazyValue<Map<String, EntityFactory>>() {
      @NotNull
      @Override
      protected Map<String, EntityFactory> compute() {
        return ContainerUtil.<String, EntityFactory>immutableMapBuilder()
          .put(PIPE_DEC, Angular2MetadataPipeStub::createPipeStub)
          .put(COMPONENT_DEC, Angular2MetadataComponentStub::createComponentStub)
          .put(DIRECTIVE_DEC, Angular2MetadataDirectiveStub::createDirectiveStub)
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

  protected final Map<String, String> myInputMappings;
  protected final Map<String, String> myOutputMappings;

  public Angular2MetadataClassStubBase(@Nullable String memberName,
                                       @Nullable StubElement parent,
                                       @Nullable JsonObject source,
                                       @NotNull MetadataElementType elementType) {
    super(memberName, parent, elementType);
    if (source == null) {
      myInputMappings = Collections.emptyMap();
      myOutputMappings = Collections.emptyMap();
      return;
    }
    JsonObject extendsClass = getPropertyValue(source.findProperty(EXTENDS), JsonObject.class);
    if (extendsClass != null) {
      Angular2MetadataReferenceStub.createReferenceStub(EXTENDS_MEMBER, extendsClass, this);
    }
    myOutputMappings = new HashMap<>();
    myInputMappings = new HashMap<>();
    MetadataUtils.streamObjectProperty(source.findProperty(MEMBERS))
      .forEach(this::loadMember);
  }

  public Angular2MetadataClassStubBase(@NotNull StubInputStream stream,
                                       @Nullable StubElement parent, @NotNull MetadataElementType elementType) throws IOException {
    super(stream, parent, elementType);
    myInputMappings = readStringMap(stream);
    myOutputMappings = readStringMap(stream);
  }

  @Nullable
  public String getClassName() {
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

  @Override
  public void serialize(@NotNull StubOutputStream stream) throws IOException {
    super.serialize(stream);
    writeStringMap(myInputMappings, stream);
    writeStringMap(myOutputMappings, stream);
  }

  private void loadMember(JsonProperty property) {
    String name = property.getName();
    JsonArray val = tryCast(property.getValue(), JsonArray.class);
    if (val == null || val.getValueList().size() != 1) {
      return;
    }
    JsonObject obj = tryCast(val.getValueList().get(0), JsonObject.class);
    if (obj == null) {
      return;
    }
    if (!SYMBOL_PROPERTY.equals(readStringPropertyValue(obj.findProperty(SYMBOL_TYPE)))) {
      return;
    }
    streamDecorators(obj).forEach(dec -> {
      if (INPUT_DEC.equals(dec.first)) {
        addBindingMapping(name, myInputMappings, getDecoratorInitializer(dec.second, JsonStringLiteral.class));
      }
      else if (OUTPUT_DEC.equals(dec.first)) {
        addBindingMapping(name, myOutputMappings, getDecoratorInitializer(dec.second, JsonStringLiteral.class));
      }
    });
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
