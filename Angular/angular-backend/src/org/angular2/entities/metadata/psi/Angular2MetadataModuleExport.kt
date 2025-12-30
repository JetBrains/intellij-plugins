// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.angular2.entities.metadata.stubs.Angular2MetadataModuleExportStub
import org.angular2.lang.metadata.psi.MetadataElement
import org.jetbrains.annotations.NonNls

class Angular2MetadataModuleExport(element: Angular2MetadataModuleExportStub)
  : Angular2MetadataElement<Angular2MetadataModuleExportStub>(element) {

  private val exportNodeModule: Angular2MetadataNodeModule?
    get() {
      val from = stub.from ?: return null
      return CachedValuesManager.getCachedValue(this) {
        CachedValueProvider.Result.create(
          ExternalNodeModuleResolver(this, from, null)
            .resolve() as? Angular2MetadataNodeModule,
          PsiModificationTracker.MODIFICATION_COUNT)
      }
    }

  fun findExport(name: String?): MetadataElement<*>? {
    val mappedName = if (stub.exportMappings.isEmpty()) name else stub.exportMappings[name]
    return if (mappedName != null)
      exportNodeModule?.findMember(mappedName)
    else
      null
  }

  override fun toString(): String {
    @NonNls
    val result = StringBuilder()
    result.append("export ")
    if (!stub.exportMappings.isEmpty()) {
      result.append("{")
      stub.exportMappings.entries
        .joinTo(result, ", ") { e -> e.value + " as " + e.key } //NON-NLS
      result.append("}")
    }
    else {
      result.append("*")
    }
    return result.append(" from ")
      .append(stub.from)
      .append(" <metadata module export>").toString()
  }
}
