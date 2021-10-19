// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.lang.javascript.index.flags.BooleanStructureElement;
import com.intellij.lang.javascript.index.flags.FlagsStructure;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.SmartList;
import org.angular2.entities.Angular2DirectiveKind;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataComponent;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.psi.Angular2HtmlRecursiveElementWalkingVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.angular2.Angular2DecoratorUtil.TEMPLATE_PROP;
import static org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue;
import static org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.ATTR_SELECT;
import static org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.ELEMENT_NG_CONTENT;

public class Angular2MetadataComponentStub extends Angular2MetadataDirectiveStubBase<Angular2MetadataComponent> {

  private static final BooleanStructureElement HAS_NG_CONTENT_SELECTORS = new BooleanStructureElement();

  protected static final FlagsStructure FLAGS_STRUCTURE = new FlagsStructure(
    Angular2MetadataDirectiveStubBase.FLAGS_STRUCTURE,
    HAS_NG_CONTENT_SELECTORS
  );

  private final List<String> myNgContentSelectors;

  public Angular2MetadataComponentStub(@Nullable String memberName,
                                       @Nullable StubElement parent,
                                       @NotNull JsonObject source,
                                       @NotNull JsonObject decoratorSource) {
    super(memberName, parent, source, decoratorSource, Angular2MetadataElementTypes.COMPONENT);
    JsonObject initializer = getDecoratorInitializer(decoratorSource, JsonObject.class);
    String template;
    if (initializer == null
        || (template = readStringPropertyValue(initializer.findProperty(TEMPLATE_PROP))) == null
        || !template.contains("<" + ELEMENT_NG_CONTENT)) {
      myNgContentSelectors = Collections.emptyList();
      return;
    }
    PsiFile file = PsiFileFactory.getInstance(source.getProject())
      .createFileFromText(Angular2HtmlLanguage.INSTANCE, template);
    myNgContentSelectors = new SmartList<>();
    if (file != null) {
      file.accept(new Angular2HtmlRecursiveElementWalkingVisitor() {
        @Override
        public void visitXmlAttribute(XmlAttribute attribute) {
          if (attribute.getName().equals(ATTR_SELECT)
              && attribute.getParent().getName().equals(ELEMENT_NG_CONTENT)) {
            String value = attribute.getValue();
            if (!StringUtil.isEmptyOrSpaces(value)) {
              myNgContentSelectors.add(value);
            }
          }
        }
      });
    }
  }

  public Angular2MetadataComponentStub(@NotNull StubInputStream stream,
                                       @Nullable StubElement parent) throws IOException {
    super(stream, parent, Angular2MetadataElementTypes.COMPONENT);
    myNgContentSelectors = readFlag(HAS_NG_CONTENT_SELECTORS) ? readStringList(stream)
                                                              : Collections.emptyList();
  }

  @Override
  public @Nullable Angular2DirectiveKind getDirectiveKind() {
    return Angular2DirectiveKind.REGULAR;
  }

  public @NotNull List<String> getNgContentSelectors() {
    return myNgContentSelectors;
  }

  @Override
  public void serialize(@NotNull StubOutputStream stream) throws IOException {
    writeFlag(HAS_NG_CONTENT_SELECTORS, !myNgContentSelectors.isEmpty());
    super.serialize(stream);
    if (!myNgContentSelectors.isEmpty()) {
      writeStringList(myNgContentSelectors, stream);
    }
  }

  @Override
  protected FlagsStructure getFlagsStructure() {
    return FLAGS_STRUCTURE;
  }
}
