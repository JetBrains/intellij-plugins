// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.codeInspection.ProblemHighlightType
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.entities.Angular2Directive

object Angular2InspectionUtils {
  internal fun getBaseProblemHighlightType(scope: Angular2DeclarationsScope, matchedDirectives: List<Angular2Directive>): ProblemHighlightType {
    // TODO take into account 'CUSTOM_ELEMENTS_SCHEMA' and 'NO_ERRORS_SCHEMA' value of '@NgModule.schemas'/'@Component.schemas'
    return if (scope.isFullyResolved && matchedDirectives.none { !it.areHostDirectivesFullyResolved() })
      ProblemHighlightType.GENERIC_ERROR_OR_WARNING
    else
      ProblemHighlightType.WEAK_WARNING
  }
}
