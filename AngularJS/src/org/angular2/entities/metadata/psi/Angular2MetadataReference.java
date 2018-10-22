// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.util.ObjectUtils;
import org.angular2.entities.metadata.stubs.Angular2MetadataReferenceStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2MetadataReference extends Angular2MetadataElement<Angular2MetadataReferenceStub> {
  public Angular2MetadataReference(@NotNull Angular2MetadataReferenceStub element) {
    super(element);
  }

  @Nullable
  public Angular2MetadataElement resolve() {
    String module = getStub().getModule();
    if (module != null) {
      //XXX implement cross-module references
      return null;
    }
    return ObjectUtils.tryCast(getNodeModule().findMember(getStub().getName()), Angular2MetadataElement.class);
  }


}
