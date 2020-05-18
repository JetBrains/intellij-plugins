// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.inspections;

import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.lang.javascript.JSAnalysisHandlersFactory;
import org.jetbrains.annotations.NotNull;

public class AngularJSAnalysisHandlersFactory extends JSAnalysisHandlersFactory {

  @Override
  public @NotNull InspectionSuppressor getInspectionSuppressor() {
    return AngularJSInspectionSuppressor.INSTANCE;
  }
}
