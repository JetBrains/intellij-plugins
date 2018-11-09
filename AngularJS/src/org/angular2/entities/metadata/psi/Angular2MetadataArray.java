// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import org.angular2.entities.metadata.stubs.Angular2MetadataArrayStub;
import org.jetbrains.annotations.NotNull;

public class Angular2MetadataArray extends Angular2MetadataElement<Angular2MetadataArrayStub> {
  public Angular2MetadataArray(@NotNull Angular2MetadataArrayStub element) {
    super(element);
  }

  @Override
  public String toString() {
    return (getStub().getMemberName() != null ? getStub().getMemberName() + " " : "") + "<metadata array>";
  }
}
