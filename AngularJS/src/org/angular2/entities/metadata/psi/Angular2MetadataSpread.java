// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import org.angular2.entities.metadata.stubs.Angular2MetadataElementStub;
import org.angular2.entities.metadata.stubs.Angular2MetadataSpreadStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.doIfNotNull;

public class Angular2MetadataSpread extends Angular2MetadataElement<Angular2MetadataSpreadStub> {

  public Angular2MetadataSpread(@NotNull Angular2MetadataSpreadStub element) {
    super(element);
  }

  public @Nullable Angular2MetadataElement getExpression() {
    return doIfNotNull(getStub().getSpreadExpression(), Angular2MetadataElementStub::getPsi);
  }

  @Override
  public String toString() {
    String memberName = getStub().getMemberName();
    return (memberName == null ? "" : memberName + ": ")
           + "<metadata spread>";
  }
}
