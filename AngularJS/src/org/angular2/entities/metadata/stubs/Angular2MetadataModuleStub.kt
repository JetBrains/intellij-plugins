// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.JsonObject
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.angular2.Angular2DecoratorUtil.DECLARATIONS_PROP
import org.angular2.Angular2DecoratorUtil.EXPORTS_PROP
import org.angular2.Angular2DecoratorUtil.IMPORTS_PROP
import org.angular2.entities.metadata.Angular2MetadataElementTypes
import org.angular2.entities.metadata.psi.Angular2MetadataModule
import org.angular2.index.Angular2IndexingHandler
import org.angular2.index.Angular2MetadataModuleIndex
import java.io.IOException

class Angular2MetadataModuleStub : Angular2MetadataEntityStub<Angular2MetadataModule> {

  constructor(memberName: String?, parent: StubElement<*>?, classSource: JsonObject, decoratorSource: JsonObject)
    : super(memberName, parent, classSource, Angular2MetadataElementTypes.MODULE) {

    val initializer = getDecoratorInitializer<JsonObject>(decoratorSource)
    if (initializer != null) {
      stubDecoratorFields(initializer, *STUBBED_DECORATOR_FIELDS)
    }
  }

  @Throws(IOException::class)
  constructor(stream: StubInputStream, parent: StubElement<*>?)
    : super(stream, parent, Angular2MetadataElementTypes.MODULE)

  override fun index(sink: IndexSink) {
    super.index(sink)
    sink.occurrence(Angular2MetadataModuleIndex.KEY,
                    Angular2IndexingHandler.NG_MODULE_INDEX_NAME)
  }

  override val loadInOuts: Boolean
    get() = false

  companion object {

    private val STUBBED_DECORATOR_FIELDS = arrayOf(DECLARATIONS_PROP, EXPORTS_PROP, IMPORTS_PROP)
  }
}
