package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.internal.statistic.eventLog.validator.rules.impl.CustomValidationRule
import com.jetbrains.fus.reporting.api.IEventContext
import com.jetbrains.fus.reporting.api.ValidationResultType

class InspectionIdValidationRule : CustomValidationRule() {
  override fun getRuleId(): String = "inspection_id_rule"

  override fun doValidate(data: String, context: IEventContext): ValidationResultType =
    if (data == FLEXINSPECT_STATS_INSPECTION_ID || isThirdPartyValue(data)) ValidationResultType.ACCEPTED
    else acceptWhenReportedByPluginFromPluginRepository(context)
}
