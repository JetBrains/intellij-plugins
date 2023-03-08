// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction
import com.intellij.model.Pointer
import com.intellij.refactoring.suggested.createSmartPointer
import org.angular2.entities.Angular2EntityUtils
import org.angular2.entities.Angular2Pipe
import org.angular2.entities.metadata.stubs.Angular2MetadataPipeStub

class Angular2MetadataPipe(element: Angular2MetadataPipeStub) : Angular2MetadataDeclaration<Angular2MetadataPipeStub>(
  element), Angular2Pipe {

  override val transformMethods: Collection<TypeScriptFunction>
    get() = getCachedClassBasedValue { cls ->
      if (cls != null)
        Angular2EntityUtils.getPipeTransformMethods(cls)
      else
        emptyList()
    }

  override fun getName(): String = stub.pipeName

  override fun createPointer(): Pointer<out Angular2Pipe> {
    return this.createSmartPointer()
  }
}
