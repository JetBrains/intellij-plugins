// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import org.angular2.entities.metadata.stubs.Angular2MetadataDirectiveStub;
import org.jetbrains.annotations.NotNull;

public class Angular2MetadataDirective extends Angular2MetadataDirectiveBase<Angular2MetadataDirectiveStub> {
  public Angular2MetadataDirective(@NotNull Angular2MetadataDirectiveStub element) {
    super(element);
  }

  @Override
  public boolean isTemplate() {
    return getStub().isTemplate();
  }
}
