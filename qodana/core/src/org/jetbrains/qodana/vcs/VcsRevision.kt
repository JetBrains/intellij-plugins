package org.jetbrains.qodana.vcs

import java.time.Instant

internal data class VcsRevision(
  val id: String,
  val date: Instant
)