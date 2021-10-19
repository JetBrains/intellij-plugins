// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.model.Pointer;
import org.angular2.web.Angular2Symbol;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface Angular2Directive extends Angular2Declaration {

  Angular2Directive[] EMPTY_ARRAY = new Angular2Directive[0];

  @NotNull Pointer<? extends Angular2Directive> createPointer();

  @NotNull Angular2DirectiveSelector getSelector();

  @NotNull List<String> getExportAsList();

  default @NotNull Collection<? extends Angular2DirectiveProperty> getInputs() {
    return getBindings().getInputs();
  }

  default @NotNull Collection<? extends Angular2DirectiveProperty> getOutputs() {
    return getBindings().getOutputs();
  }

  default List<? extends Angular2Symbol> getInOuts() {
    return getBindings().getInOuts();
  }

  @NotNull Angular2DirectiveProperties getBindings();

  @NotNull Collection<? extends Angular2DirectiveAttribute> getAttributes();

  @NotNull Angular2DirectiveKind getDirectiveKind();

  default boolean isComponent() {
    return false;
  }
}
