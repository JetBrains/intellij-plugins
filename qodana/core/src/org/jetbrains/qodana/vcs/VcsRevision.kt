package org.jetbrains.qodana.vcs

import java.time.Instant

data class VcsRevision(
  val id: String,
  val date: Instant
)