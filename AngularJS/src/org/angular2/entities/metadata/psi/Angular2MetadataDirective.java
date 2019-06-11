// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.util.containers.hash.HashSet;
import org.angular2.entities.metadata.stubs.Angular2MetadataClassStubBase;
import org.angular2.entities.metadata.stubs.Angular2MetadataDirectiveStub;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Angular2MetadataDirective extends Angular2MetadataDirectiveBase<Angular2MetadataDirectiveStub> {
  public Angular2MetadataDirective(@NotNull Angular2MetadataDirectiveStub element) {
    super(element);
  }

  @Override
  public boolean isStructuralDirective() {
    Angular2MetadataClassBase cur = this;
    Set<Angular2MetadataClassBase> visited = new HashSet<>();
    while (cur != null && visited.add(cur)) {
      if (((Angular2MetadataClassStubBase)cur.getStub()).isStructuralDirective()) {
        return true;
      }
      cur = cur.getExtendedClass();
    }
    return false;
  }

  @Override
  public boolean isRegularDirective() {
    Angular2MetadataClassBase cur = this;
    Set<Angular2MetadataClassBase> visited = new HashSet<>();
    while (cur != null && visited.add(cur)) {
      if (((Angular2MetadataClassStubBase)cur.getStub()).isStructuralDirective()
          && !((Angular2MetadataClassStubBase)cur.getStub()).isRegularDirective()) {
        return false;
      }
      cur = cur.getExtendedClass();
    }
    return true;
  }
}
