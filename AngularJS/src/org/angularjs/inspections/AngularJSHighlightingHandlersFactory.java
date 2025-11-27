// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.inspections;

import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.lang.javascript.JSHighlightingHandlersFactory;
import org.jetbrains.annotations.NotNull;

public final class AngularJSHighlightingHandlersFactory extends JSHighlightingHandlersFactory {

  @Override
  public @NotNull InspectionSuppressor getInspectionSuppressor() {
    return AngularJSInspectionSuppressor.INSTANCE;
  }
}
