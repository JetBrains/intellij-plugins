// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.model.Pointer;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveKind;
import org.angular2.entities.metadata.stubs.Angular2MetadataDirectiveStub;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.intellij.refactoring.suggested.UtilsKt.createSmartPointer;

public class Angular2MetadataDirective extends Angular2MetadataDirectiveBase<Angular2MetadataDirectiveStub> {
  public Angular2MetadataDirective(@NotNull Angular2MetadataDirectiveStub element) {
    super(element);
  }

  @Override
  public @NotNull Pointer<? extends Angular2Directive> createPointer() {
    return createSmartPointer(this);
  }

  @Override
  public @NotNull Angular2DirectiveKind getDirectiveKind() {
    Angular2MetadataClassBase<?> cur = this;
    Set<Angular2MetadataClassBase<?>> visited = new HashSet<>();
    while (cur != null && visited.add(cur)) {
      Angular2DirectiveKind result = cur.getStub().getDirectiveKind();
      if (result != null) {
        return result;
      }
      cur = cur.getExtendedClass();
    }
    return Angular2DirectiveKind.REGULAR;
  }
}
