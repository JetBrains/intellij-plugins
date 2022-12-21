// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import org.angular2.entities.metadata.stubs.Angular2MetadataStringStub;
import org.jetbrains.annotations.NotNull;

public class Angular2MetadataString extends Angular2MetadataElement<Angular2MetadataStringStub> {

  public Angular2MetadataString(@NotNull Angular2MetadataStringStub element) {
    super(element);
  }

  public @NotNull String getValue() {
    return getStub().getValue();
  }

  @Override
  public String toString() {
    String memberName = getStub().getMemberName();
    return (memberName == null ? "" : memberName + ": ")
           + getValue() + " <metadata string>";
  }
}
