// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import org.angular2.entities.metadata.stubs.Angular2MetadataClassStub;
import org.jetbrains.annotations.NotNull;

public class Angular2MetadataClass extends Angular2MetadataElement<Angular2MetadataClassStub> {

  public Angular2MetadataClass(@NotNull Angular2MetadataClassStub element) {
    super(element);
  }

  public String getClassName() {
    return getStub().getClassName();
  }

}
