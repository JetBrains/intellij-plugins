package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.internal.statistic.eventLog.validator.ValidationResultType
import com.intellij.internal.statistic.eventLog.validator.rules.EventContext
import com.intellij.internal.statistic.eventLog.validator.rules.impl.CustomValidationRule

class InspectionIdValidationRule : CustomValidationRule() {
  override fun getRuleId(): String = "inspection_id_rule"

  override fun doValidate(data: String, context: EventContext): ValidationResultType {
    return if (data == FLEXINSPECT_STATS_INSPECTION_ID || isThirdPartyValue(data)) {
      ValidationResultType.ACCEPTED
    } else {
      acceptWhenReportedByPluginFromPluginRepository(context)
    }
  }
}