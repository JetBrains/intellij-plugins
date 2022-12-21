// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import org.angular2.entities.metadata.stubs.Angular2MetadataCallStub;
import org.angular2.entities.metadata.stubs.Angular2MetadataElementStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

import static com.intellij.util.ObjectUtils.doIfNotNull;

public class Angular2MetadataCall extends Angular2MetadataElement<Angular2MetadataCallStub> {

  public Angular2MetadataCall(@NotNull Angular2MetadataCallStub element) {
    super(element);
  }

  public @Nullable Angular2MetadataElement getValue() {
    Angular2MetadataElement callResult = doIfNotNull(getStub().getCallResult(), Angular2MetadataElementStub::getPsi);
    Set<Angular2MetadataElement> refs = new HashSet<>();
    while (callResult instanceof Angular2MetadataReference && refs.add(callResult)) {
      callResult = ((Angular2MetadataReference)callResult).resolve();
    }
    if (callResult instanceof Angular2MetadataFunction) {
      return ((Angular2MetadataFunction)callResult).getValue();
    }
    return null;
  }

  @Override
  public String toString() {
    String memberName = getStub().getMemberName();
    return (memberName == null ? "" : memberName + ": ")
           + "<metadata call>";
  }
}
