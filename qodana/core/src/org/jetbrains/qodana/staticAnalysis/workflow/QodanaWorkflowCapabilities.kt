package org.jetbrains.qodana.staticAnalysis.workflow

import org.jetbrains.annotations.ApiStatus

@JvmInline
value class QodanaWorkflowCapability(val id: String) {
  override fun toString(): String = id
}

@ApiStatus.Internal
object QodanaWorkflowCapabilities {
  val JdkRecovery: QodanaWorkflowCapability = QodanaWorkflowCapability("qodana.jvm.jdk.recovery")
}
