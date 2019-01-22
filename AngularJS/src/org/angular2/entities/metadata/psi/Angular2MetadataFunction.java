// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.util.ObjectUtils;
import org.angular2.entities.metadata.stubs.Angular2MetadataFunctionStub;
import org.angular2.entities.metadata.stubs.Angular2MetadataReferenceStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2MetadataFunction extends Angular2MetadataElement<Angular2MetadataFunctionStub> {
  public Angular2MetadataFunction(@NotNull Angular2MetadataFunctionStub element) {
    super(element);
  }

  @Override
  public String toString() {
    return (getStub().getMemberName() != null ? getStub().getMemberName() + " " : "") + "<metadata function>";
  }

  @Nullable
  public Angular2MetadataModule getReferencedModule() {
    Angular2MetadataReferenceStub referenceStub = getStub().getNgModuleReference();
    if (referenceStub == null) {
      return null;
    }
    return ObjectUtils.tryCast(referenceStub.getPsi().resolve(), Angular2MetadataModule.class);
  }
}
