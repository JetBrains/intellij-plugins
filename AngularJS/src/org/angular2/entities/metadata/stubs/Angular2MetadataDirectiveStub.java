// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.javascript.index.flags.BooleanStructureElement;
import com.intellij.lang.javascript.index.flags.FlagsStructure;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataDirective;
import org.angular2.index.Angular2IndexingHandler;
import org.angular2.lang.metadata.MetadataUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static org.angular2.Angular2DecoratorUtil.SELECTOR_PROP;

public class Angular2MetadataDirectiveStub extends Angular2MetadataDirectiveStubBase<Angular2MetadataDirective> {

  @Nullable
  public static Angular2MetadataDirectiveStub createDirectiveStub(@Nullable String memberName,
                                                                  @Nullable StubElement parent,
                                                                  @NotNull JsonObject classSource,
                                                                  @NotNull JsonObject decoratorSource) {
    JsonObject decoratorArg = getDecoratorInitializer(decoratorSource, JsonObject.class);
    if (decoratorArg != null) {
      String selector = MetadataUtils.readStringPropertyValue(decoratorArg.findProperty(SELECTOR_PROP));
      if (StringUtil.isNotEmpty(selector)) {
        return new Angular2MetadataDirectiveStub(memberName, parent, classSource, decoratorArg);
      }
    }
    return null;
  }

  private static final BooleanStructureElement IS_TEMPLATE_FLAG = new BooleanStructureElement();
  @SuppressWarnings("StaticFieldReferencedViaSubclass")
  protected static final FlagsStructure FLAGS_STRUCTURE = new FlagsStructure(
    Angular2MetadataDirectiveStubBase.FLAGS_STRUCTURE,
    IS_TEMPLATE_FLAG
  );

  public Angular2MetadataDirectiveStub(@Nullable String memberName,
                                       @Nullable StubElement parent,
                                       @NotNull JsonObject source,
                                       @NotNull JsonObject initializer) {
    super(memberName, parent, source, initializer, Angular2MetadataElementTypes.DIRECTIVE);
    loadDirective(source);
  }

  public Angular2MetadataDirectiveStub(@NotNull StubInputStream stream,
                                       @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.DIRECTIVE);
  }

  @Override
  public boolean isTemplate() {
    return readFlag(IS_TEMPLATE_FLAG);
  }

  @Override
  protected FlagsStructure getFlagsStructure() {
    return FLAGS_STRUCTURE;
  }

  private void loadDirective(JsonObject source) {
    JsonObject members = tryCast(doIfNotNull(source.findProperty("members"), JsonProperty::getValue), JsonObject.class);
    JsonProperty constructor = members != null ? members.findProperty(CONSTRUCTOR) : null;
    writeFlag(IS_TEMPLATE_FLAG, constructor != null
                                && constructor.getText().contains(Angular2IndexingHandler.TEMPLATE_REF));
  }
}
