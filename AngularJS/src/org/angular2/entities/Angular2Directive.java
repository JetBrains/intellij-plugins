// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface Angular2Directive extends Angular2Declaration {

  Angular2Directive[] EMPTY_ARRAY = new Angular2Directive[0];

  @NotNull Angular2DirectiveSelector getSelector();

  @NotNull List<String> getExportAsList();

  default @NotNull Collection<? extends Angular2DirectiveProperty> getInputs() {
    return getBindings().getInputs();
  }

  default @NotNull Collection<? extends Angular2DirectiveProperty> getOutputs() {
    return getBindings().getOutputs();
  }

  default List<Pair<Angular2DirectiveProperty, Angular2DirectiveProperty>> getInOuts() {
    return getBindings().getInOuts();
  }

  @NotNull Angular2DirectiveProperties getBindings();

  @NotNull Collection<? extends Angular2DirectiveAttribute> getAttributes();

  @NotNull Angular2DirectiveKind getDirectiveKind();

  default boolean isComponent() {
    return this instanceof Angular2Component;
  }
}
