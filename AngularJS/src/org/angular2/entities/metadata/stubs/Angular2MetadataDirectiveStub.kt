// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.JsonObject
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.angular2.entities.metadata.Angular2MetadataElementTypes
import org.angular2.entities.metadata.psi.Angular2MetadataDirective

import java.io.IOException

class Angular2MetadataDirectiveStub : Angular2MetadataDirectiveStubBase<Angular2MetadataDirective> {

  constructor(memberName: String?, parent: StubElement<*>?, source: JsonObject, decoratorSource: JsonObject)
    : super(memberName, parent, source, decoratorSource, Angular2MetadataElementTypes.DIRECTIVE)

  @Throws(IOException::class)
  constructor(stream: StubInputStream, parent: StubElement<*>?)
    : super(stream, parent, Angular2MetadataElementTypes.DIRECTIVE)
}
