// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.stubs

import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import org.angular2.lang.metadata.psi.MetadataElementType
import org.angular2.lang.metadata.stubs.MetadataElementStub
import org.angular2.lang.metadata.stubs.MetadataElementStub.ConstructorFromJsonValue
import java.io.IOException

abstract class Angular2MetadataElementStub<Psi : PsiElement> : MetadataElementStub<Psi> {

  constructor(memberName: String?, parent: StubElement<*>?, elementType: MetadataElementType<*>) : super(memberName, parent, elementType)

  @Throws(IOException::class)
  constructor(stream: StubInputStream,
              parent: StubElement<*>?,
              elementType: MetadataElementType<*>) : super(stream, parent, elementType)

  override val typeFactory: Map<String, ConstructorFromJsonValue>
    get() = TYPE_FACTORY.value

  companion object {
    @JvmStatic
    protected val FLAGS_STRUCTURE = MetadataElementStub.FLAGS_STRUCTURE

    private val TYPE_FACTORY = NotNullLazyValue.lazy {
      mapOf(
        SYMBOL_CLASS to ConstructorFromJsonValue { memberName, source, parent ->
          Angular2MetadataClassStubBase.createClassStub(memberName, source, parent)
        },
        SYMBOL_REFERENCE to ConstructorFromJsonValue { memberName, source, parent ->
          Angular2MetadataReferenceStub.createReferenceStub(memberName, source, parent)
        },
        SYMBOL_FUNCTION to ConstructorFromJsonValue { memberName, source, parent ->
          Angular2MetadataFunctionStub.createFunctionStub(memberName, source, parent)
        },
        SYMBOL_CALL to ConstructorFromJsonValue { memberName, source, parent ->
          Angular2MetadataCallStub.createCallStub(memberName, source, parent)
        },
        SYMBOL_SPREAD to ConstructorFromJsonValue { memberName, source, parent ->
          Angular2MetadataSpreadStub.createSpreadStub(memberName, source, parent)
        },
        STRING_TYPE to ConstructorFromJsonValue { memberName, source, parent ->
          Angular2MetadataStringStub(memberName, source, parent)
        },
        ARRAY_TYPE to ConstructorFromJsonValue { memberName, source, parent ->
          Angular2MetadataArrayStub(memberName, source, parent)
        },
        OBJECT_TYPE to ConstructorFromJsonValue { memberName, source, parent ->
          Angular2MetadataObjectStub(memberName, source, parent)
        }
      )
    }
  }
}
