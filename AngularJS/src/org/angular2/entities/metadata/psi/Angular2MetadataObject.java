// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import org.angular2.entities.metadata.stubs.Angular2MetadataObjectStub;
import org.jetbrains.annotations.NotNull;

public class Angular2MetadataObject extends Angular2MetadataElement<Angular2MetadataObjectStub> {
  public Angular2MetadataObject(@NotNull Angular2MetadataObjectStub element) {
    super(element);
  }

  @Override
  public String toString() {
    return (getStub().getMemberName() != null ? getStub().getMemberName() + " " : "") + "<metadata object>";
  }
}
