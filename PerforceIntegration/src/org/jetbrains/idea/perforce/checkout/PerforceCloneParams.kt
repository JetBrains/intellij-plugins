// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.perforce.checkout

internal data class PerforceCloneParams(
  val server: String,
  val user: String,
  val password: String,
  val client: String,
  val directory: String,
)
