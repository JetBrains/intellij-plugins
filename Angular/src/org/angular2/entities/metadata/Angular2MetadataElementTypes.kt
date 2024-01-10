// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata

import org.angular2.entities.metadata.psi.*
import org.angular2.entities.metadata.stubs.*
import org.angular2.lang.metadata.MetadataJsonLanguage
import org.angular2.lang.metadata.psi.MetadataElementType
import org.jetbrains.annotations.NonNls

interface Angular2MetadataElementTypes {

  class Angular2MetadataElementType<Stub : Angular2MetadataElementStub<*>>(
    @NonNls debugName: String,
    stubConstructor: MetadataStubConstructor<out Stub>,
    psiConstructor: MetadataElementConstructor<Stub>
  ) : MetadataElementType<Stub>(debugName, MetadataJsonLanguage, stubConstructor, psiConstructor) {

    @NonNls
    override fun toString(): String {
      return EXTERNAL_PREFIX_ID + super.getDebugName()
    }

  }

  @Suppress("MayBeConstant")
  companion object {

    @JvmField
    val STUB_VERSION = 1

    @JvmField
    val EXTERNAL_PREFIX_ID = "NG-META:"

    @JvmField
    val STRING: MetadataElementType<Angular2MetadataStringStub> = Angular2MetadataElementType(
      "STRING",
      { stream, parent -> Angular2MetadataStringStub(stream, parent) },
      { element -> Angular2MetadataString(element) })

    @JvmField
    val ARRAY: MetadataElementType<Angular2MetadataArrayStub> = Angular2MetadataElementType(
      "ARRAY",
      { stream, parent -> Angular2MetadataArrayStub(stream, parent) },
      { element -> Angular2MetadataArray(element) })

    @JvmField
    val OBJECT: MetadataElementType<Angular2MetadataObjectStub> = Angular2MetadataElementType(
      "OBJECT",
      { stream, parent -> Angular2MetadataObjectStub(stream, parent) },
      { element -> Angular2MetadataObject(element) })

    @JvmField
    val REFERENCE: MetadataElementType<Angular2MetadataReferenceStub> = Angular2MetadataElementType(
      "REFERENCE",
      { stream, parent -> Angular2MetadataReferenceStub(stream, parent) },
      { element -> Angular2MetadataReference(element) })

    @JvmField
    val FUNCTION: MetadataElementType<Angular2MetadataFunctionStub> = Angular2MetadataElementType(
      "FUNCTION",
      { stream, parent -> Angular2MetadataFunctionStub(stream, parent) },
      { element -> Angular2MetadataFunction(element) })

    @JvmField
    val CALL: MetadataElementType<Angular2MetadataCallStub> = Angular2MetadataElementType(
      "CALL",
      { stream, parent -> Angular2MetadataCallStub(stream, parent) },
      { element -> Angular2MetadataCall(element) })

    @JvmField
    val SPREAD: MetadataElementType<Angular2MetadataSpreadStub> = Angular2MetadataElementType(
      "SPREAD",
      { stream, parent -> Angular2MetadataSpreadStub(stream, parent) },
      { element -> Angular2MetadataSpread(element) })

    @JvmField
    val CLASS: MetadataElementType<Angular2MetadataClassStub> = Angular2MetadataElementType(
      "CLASS",
      { stream, parent -> Angular2MetadataClassStub(stream, parent) },
      { element -> Angular2MetadataClass(element) })

    @JvmField
    val NODE_MODULE: MetadataElementType<Angular2MetadataNodeModuleStub> = Angular2MetadataElementType(
      "NODE_MODULE",
      { stream, parentStub -> Angular2MetadataNodeModuleStub(stream, parentStub) },
      { element -> Angular2MetadataNodeModule(element) })

    @JvmField
    val MODULE_EXPORT: MetadataElementType<Angular2MetadataModuleExportStub> = Angular2MetadataElementType(
      "MODULE_EXPORT",
      { stream, parent -> Angular2MetadataModuleExportStub(stream, parent) },
      { element -> Angular2MetadataModuleExport(element) })

    @JvmField
    val MODULE: MetadataElementType<Angular2MetadataModuleStub> = Angular2MetadataElementType(
      "MODULE",
      { stream, parent -> Angular2MetadataModuleStub(stream, parent) },
      { element -> Angular2MetadataModule(element) })

    @JvmField
    val PIPE: MetadataElementType<Angular2MetadataPipeStub> = Angular2MetadataElementType(
      "PIPE",
      { stream, parent -> Angular2MetadataPipeStub(stream, parent) },
      { element -> Angular2MetadataPipe(element) })

    @JvmField
    val DIRECTIVE: MetadataElementType<Angular2MetadataDirectiveStub> = Angular2MetadataElementType(
      "DIRECTIVE",
      { stream, parent -> Angular2MetadataDirectiveStub(stream, parent) },
      { element -> Angular2MetadataDirective(element) })

    @JvmField
    val COMPONENT: MetadataElementType<Angular2MetadataComponentStub> = Angular2MetadataElementType(
      "COMPONENT",
      { stream, parent -> Angular2MetadataComponentStub(stream, parent) },
      { element -> Angular2MetadataComponent(element) })
  }
}
