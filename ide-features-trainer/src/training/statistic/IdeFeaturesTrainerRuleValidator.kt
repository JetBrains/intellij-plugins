package training.statistic

import com.intellij.internal.statistic.eventLog.validator.ValidationResultType
import com.intellij.internal.statistic.eventLog.validator.rules.EventContext
import com.intellij.internal.statistic.eventLog.validator.rules.impl.CustomWhiteListRule
import training.learn.CourseManager
import training.statistic.FeatureUsageStatisticConsts.LESSON_ID

class IdeFeaturesTrainerRuleValidator : CustomWhiteListRule() {

  override fun acceptRuleId(ruleId: String?): Boolean = (ruleId == LESSON_ID)

  override fun doValidate(data: String, context: EventContext): ValidationResultType {
    return if (CourseManager.instance.modules
            .flatMap { it.lessons }
            .firstOrNull { it.id == data } != null)
      ValidationResultType.ACCEPTED
    else
      ValidationResultType.REJECTED
  }

}