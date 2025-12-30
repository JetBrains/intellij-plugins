// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.angular2.entities.metadata.stubs.Angular2MetadataReferenceStub

class Angular2MetadataReference(element: Angular2MetadataReferenceStub) : Angular2MetadataElement<Angular2MetadataReferenceStub>(element) {

  fun resolve(): Angular2MetadataElement<*>? {
    val moduleName = stub.module
    if (moduleName != null) {
      val elementName = stub.name
      return CachedValuesManager.getCachedValue(this) {
        CachedValueProvider.Result.create(
          ExternalNodeModuleResolver(this, moduleName, elementName).resolve(),
          PsiModificationTracker.MODIFICATION_COUNT)
      }
    }
    else {
      return nodeModule?.findMember(stub.name) as? Angular2MetadataElement<*>
    }
  }

  override fun toString(): String {
    val module = stub.module
    val memberName = stub.memberName
    return ((if (memberName == null) "" else "$memberName: ")
            + (if (module == null) "" else "$module#")
            + stub.name + " <metadata reference>")
  }
}
