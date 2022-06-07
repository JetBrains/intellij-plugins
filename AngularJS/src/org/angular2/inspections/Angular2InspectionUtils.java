// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.jetbrains.annotations.NotNull;

public class Angular2InspectionUtils {
  @NotNull
  static ProblemHighlightType getBaseProblemHighlightType(Angular2DeclarationsScope scope) {
    // TODO take into account 'CUSTOM_ELEMENTS_SCHEMA' and 'NO_ERRORS_SCHEMA' value of '@NgModule.schemas'/'@Component.schemas'
    return scope.isFullyResolved() ? ProblemHighlightType.GENERIC_ERROR_OR_WARNING : ProblemHighlightType.WEAK_WARNING;
  }
}
