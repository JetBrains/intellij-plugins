package org.jetbrains.qodana.webUi

import org.jetbrains.qodana.run.QodanaConverterResults

interface ActiveWebUi {
  val qodanaConverterResults: QodanaConverterResults

  val webUiId: String

  val token: String

  suspend fun close(): Boolean

  override fun hashCode(): Int

  override fun equals(other: Any?): Boolean
}