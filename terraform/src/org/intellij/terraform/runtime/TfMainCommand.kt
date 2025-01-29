// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.openapi.util.NlsSafe

internal enum class TfCommand(val command: @NlsSafe String) {
  INIT("init"),
  VALIDATE("validate"),
  PLAN("plan"),
  APPLY("apply"),
  DESTROY("destroy"),
  CUSTOM("");
}