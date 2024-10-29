// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu

object OpenTofuConstants {

  internal const val TOFU_ENCRYPTION: String = "encryption"
  internal const val TOFU_KEY_PROVIDER: String = "key_provider"
  internal const val TOFU_ENCRYPTION_METHOD_BLOCK: String = "method"
  internal const val TOFU_KEYS_PROPERTY: String = "keys"
  internal const val TOFU_STATE_BLOCK: String = "state"
  internal const val TOFU_PLAN_BLOCK: String = "plan"
  internal const val TOFU_FALLBACK_BLOCK: String = "fallback"
  internal const val TOFU_ENCRYPTION_METHOD_PROPERTY: String = "method"

  internal val OpenTofuScopes: Set<String> = setOf(TOFU_KEY_PROVIDER, TOFU_ENCRYPTION_METHOD_BLOCK)

}