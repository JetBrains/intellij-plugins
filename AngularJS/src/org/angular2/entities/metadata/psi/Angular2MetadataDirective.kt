// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import com.intellij.model.Pointer
import com.intellij.refactoring.suggested.createSmartPointer
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2DirectiveKind
import org.angular2.entities.metadata.stubs.Angular2MetadataDirectiveStub

class Angular2MetadataDirective(element: Angular2MetadataDirectiveStub) : Angular2MetadataDirectiveBase<Angular2MetadataDirectiveStub>(
  element) {

  override val directiveKind: Angular2DirectiveKind
    get() {
      var cur: Angular2MetadataClassBase<*>? = this
      val visited = HashSet<Angular2MetadataClassBase<*>>()
      while (cur != null && visited.add(cur)) {
        val result = cur.stub.directiveKind
        if (result != null) {
          return result
        }
        cur = cur.extendedClass
      }
      return Angular2DirectiveKind.REGULAR
    }

  override fun createPointer(): Pointer<out Angular2Directive> {
    return this.createSmartPointer()
  }
}
