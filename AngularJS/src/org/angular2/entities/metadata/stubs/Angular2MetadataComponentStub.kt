// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.JsonObject
import com.intellij.lang.javascript.index.flags.BooleanStructureElement
import com.intellij.lang.javascript.index.flags.FlagsStructure
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.SmartList
import org.angular2.Angular2DecoratorUtil.TEMPLATE_PROP
import org.angular2.entities.Angular2DirectiveKind
import org.angular2.entities.metadata.Angular2MetadataElementTypes
import org.angular2.entities.metadata.psi.Angular2MetadataComponent
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.psi.Angular2HtmlRecursiveElementWalkingVisitor
import org.angular2.lang.metadata.MetadataUtils.readStringPropertyValue
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.ATTR_SELECT
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.ELEMENT_NG_CONTENT
import java.io.IOException

class Angular2MetadataComponentStub : Angular2MetadataDirectiveStubBase<Angular2MetadataComponent> {

  private val myNgContentSelectors: List<String>

  override val directiveKind: Angular2DirectiveKind
    get() = Angular2DirectiveKind.REGULAR

  val ngContentSelectors: List<String>
    get() = myNgContentSelectors

  constructor(memberName: String?, parent: StubElement<*>?, source: JsonObject, decoratorSource: JsonObject)
    : super(memberName, parent, source, decoratorSource, Angular2MetadataElementTypes.COMPONENT) {
    val initializer = getDecoratorInitializer<JsonObject>(decoratorSource)
    val template = initializer?.findProperty(TEMPLATE_PROP)?.let { readStringPropertyValue(it) }
    if (initializer == null
        || template == null
        || !template.contains("<$ELEMENT_NG_CONTENT")) {
      myNgContentSelectors = emptyList()
      return
    }
    val file = PsiFileFactory.getInstance(source.project)
      .createFileFromText(Angular2HtmlLanguage.INSTANCE, template)
    myNgContentSelectors = SmartList()
    file?.accept(object : Angular2HtmlRecursiveElementWalkingVisitor() {
      override fun visitXmlAttribute(attribute: XmlAttribute) {
        if (attribute.name == ATTR_SELECT && attribute.parent.name == ELEMENT_NG_CONTENT) {
          val value = attribute.value
          if (!value.isNullOrBlank()) {
            myNgContentSelectors.add(value)
          }
        }
      }
    })
  }

  @Throws(IOException::class)
  constructor(stream: StubInputStream, parent: StubElement<*>?)
    : super(stream, parent, Angular2MetadataElementTypes.COMPONENT) {
    myNgContentSelectors = if (readFlag(HAS_NG_CONTENT_SELECTORS))
      readStringList(stream)
    else
      emptyList()
  }

  @Throws(IOException::class)
  override fun serialize(stream: StubOutputStream) {
    writeFlag(HAS_NG_CONTENT_SELECTORS, !myNgContentSelectors.isEmpty())
    super.serialize(stream)
    if (!myNgContentSelectors.isEmpty()) {
      writeStringList(myNgContentSelectors, stream)
    }
  }

  override val flagsStructure: FlagsStructure
    get() = FLAGS_STRUCTURE

  companion object {

    private val HAS_NG_CONTENT_SELECTORS = BooleanStructureElement()

    private val FLAGS_STRUCTURE = FlagsStructure(
      Angular2MetadataDirectiveStubBase.FLAGS_STRUCTURE,
      HAS_NG_CONTENT_SELECTORS
    )
  }
}
