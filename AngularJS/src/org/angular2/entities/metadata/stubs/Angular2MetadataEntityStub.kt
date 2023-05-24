// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.angular2.lang.metadata.psi.MetadataElementType
import org.jetbrains.annotations.NonNls
import java.io.IOException

open class Angular2MetadataEntityStub<Psi : PsiElement> : Angular2MetadataClassStubBase<Psi> {

  constructor(memberName: String?,
              parent: StubElement<*>?,
              source: JsonObject,
              elementType: MetadataElementType<*>) : super(memberName, parent, source, elementType)

  @Throws(IOException::class)
  constructor(stream: StubInputStream,
              parent: StubElement<*>?, elementType: MetadataElementType<*>) : super(stream, parent, elementType)

  protected fun stubDecoratorFields(initializer: JsonObject, vararg fields: String) {
    for (name in fields) {
      val property = initializer.findProperty(name)
      if (property != null) {
        createMember(DECORATOR_FIELD_PREFIX + name, property.value)
      }
    }
  }

  fun getDecoratorFieldValueStub(name: String): StubElement<*>? {
    return findMember(DECORATOR_FIELD_PREFIX + name)
  }

  companion object {
    @JvmStatic
    @NonNls
    protected val NAME = "name"

    @NonNls
    private val DECORATOR_FIELD_PREFIX = "___dec."
  }
}
