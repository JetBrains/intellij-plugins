// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.codegen

import com.intellij.lang.typescript.kolar.KolarCodeInformation.CompletionInfo
import com.intellij.lang.typescript.kolar.KolarCodeInformation.NavigationInfo
import com.intellij.lang.typescript.kolar.KolarCodeInformation.SemanticInfo
import com.intellij.lang.typescript.kolar.KolarCodeInformation.VerificationInfo
import org.jetbrains.vuejs.lang.typescript.kolar.vue.language.core.VueCodeInformation

object codeFeatures {
  val all: VueCodeInformation = VueCodeInformation(
    verification = VerificationInfo.Enabled,
    completion = CompletionInfo.Enabled,
    semantic = SemanticInfo.Enabled,
    navigation = NavigationInfo.Enabled,
  )

  val importCompletionOnly: VueCodeInformation = VueCodeInformation(
    __importCompletion = true,
  )

  val verification: VueCodeInformation = VueCodeInformation(
    verification = VerificationInfo.Enabled,
  )

  val completion: VueCodeInformation = VueCodeInformation(
    completion = CompletionInfo.Enabled,
  )

  val withoutCompletion: VueCodeInformation = VueCodeInformation(
    verification = VerificationInfo.Enabled,
    semantic = SemanticInfo.Enabled,
    navigation = NavigationInfo.Enabled,
  )

  val navigation: VueCodeInformation = VueCodeInformation(
    navigation = NavigationInfo.Enabled,
  )

  val navigationWithoutRename: VueCodeInformation = VueCodeInformation(
    navigation = NavigationInfo.WithOptions(
      shouldRename = { false },
    ),
  )

  val navigationAndCompletion: VueCodeInformation = VueCodeInformation(
    navigation = NavigationInfo.Enabled,
    completion = CompletionInfo.Enabled,
  )

  val navigationAndVerification: VueCodeInformation = VueCodeInformation(
    navigation = NavigationInfo.Enabled,
    verification = VerificationInfo.Enabled,
  )

  val withoutNavigation: VueCodeInformation = VueCodeInformation(
    verification = VerificationInfo.Enabled,
    completion = CompletionInfo.Enabled,
    semantic = SemanticInfo.Enabled,
  )

  val semanticWithoutHighlight: VueCodeInformation = VueCodeInformation(
    semantic = SemanticInfo.WithOptions(
      shouldHighlight = { false },
    ),
  )

  val withoutHighlight: VueCodeInformation = VueCodeInformation(
    verification = VerificationInfo.Enabled,
    completion = CompletionInfo.Enabled,
    semantic = SemanticInfo.WithOptions(
      shouldHighlight = { false },
    ),
    navigation = NavigationInfo.Enabled,
  )

  val withoutHighlightAndCompletion: VueCodeInformation = VueCodeInformation(
    verification = VerificationInfo.Enabled,
    semantic = SemanticInfo.WithOptions(
      shouldHighlight = { false },
    ),
    navigation = NavigationInfo.Enabled,
  )

  val withoutSemantic: VueCodeInformation = VueCodeInformation(
    verification = VerificationInfo.Enabled,
    completion = CompletionInfo.Enabled,
    navigation = NavigationInfo.Enabled,
  )

  val doNotReportTs2339AndTs2551: VueCodeInformation = VueCodeInformation(
    verification = VerificationInfo.WithFilter(
      shouldReport = { _, code -> code != "2339" && code != "2551" },
    ),
  )

  val doNotReportTs2353AndTs2561: VueCodeInformation = VueCodeInformation(
    verification = VerificationInfo.WithFilter(
      shouldReport = { _, code -> code != "2353" && code != "2561" },
    ),
  )

  val doNotReportTs6133: VueCodeInformation = VueCodeInformation(
    verification = VerificationInfo.WithFilter(
      shouldReport = { _, code -> code != "6133" },
    ),
  )
}
