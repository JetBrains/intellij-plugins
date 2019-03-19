package training.statistic

import com.intellij.internal.statistic.beans.UsageDescriptor
import com.intellij.internal.statistic.service.fus.collectors.ApplicationUsagesCollector

class LearnApplicationUsagesCollector: ApplicationUsagesCollector() {

  override fun getUsages(): MutableSet<UsageDescriptor> {
    val usages = mutableSetOf<UsageDescriptor>()
    StatisticBase.instance.sessionLessonId2State.forEach {
      val id = it.first.sanitizeId()
      when(it.second.state) {
        StatisticBase.StatisticState.STARTED -> { usages.add(UsageDescriptor("$id.started", 1)) }
        StatisticBase.StatisticState.PASSED -> { usages.add(UsageDescriptor("$id.passed", 1, "duration:${it.second.timestamp}")) }
        else -> { /* do nothing */}
      }
    }
    return usages
  }

  override fun getGroupId(): String = "ide-features-trainer"

  fun String.sanitizeId(): String {
    return this.toLowerCase()
        .replace(" ", "")
        .replace("/", "")
        .replace("-", "")
        .replace("_", "")
  }

}