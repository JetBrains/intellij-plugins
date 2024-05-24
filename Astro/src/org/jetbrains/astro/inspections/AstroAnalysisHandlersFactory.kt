// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.inspections

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.lang.javascript.JSAnalysisHandlersFactory

class AstroAnalysisHandlersFactory : JSAnalysisHandlersFactory() {
  override fun getInspectionSuppressor(): InspectionSuppressor {
    return AstroDefaultInspectionSuppressor.INSTANCE
  }
}