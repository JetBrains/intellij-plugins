// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter
import com.intellij.javascript.testFramework.isTypeScriptServiceHighlightingInfo
import com.intellij.polySymbols.testFramework.HybridTestCase
import com.intellij.testFramework.ExtensionTestUtil

internal fun HybridTestCase.disableTypeScriptServiceWarnings() {
  ExtensionTestUtil.addExtensions(
    pointName = HighlightInfoFilter.EXTENSION_POINT_NAME,
    extensionsToAdd = listOf(
      HighlightInfoFilter { info, _ ->
        !isTypeScriptServiceHighlightingInfo(info)
      }
    ),
    parentDisposable = testRootDisposable,
  )
}
