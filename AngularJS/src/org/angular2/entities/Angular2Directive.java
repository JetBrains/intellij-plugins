// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface Angular2Directive extends Angular2Declaration {

  @Nullable
  String getSelector();

  @Nullable
  String getExportAs();

  @NotNull
  Collection<? extends Angular2DirectiveProperty> getInputs();

  @NotNull
  Collection<? extends Angular2DirectiveProperty> getOutputs();
}
