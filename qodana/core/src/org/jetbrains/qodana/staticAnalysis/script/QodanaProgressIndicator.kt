package org.jetbrains.qodana.staticAnalysis.script

import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.progress.util.ProgressIndicatorWithDelayedPresentation
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaMessageReporter
import org.jetbrains.qodana.staticAnalysis.inspections.runner.isInteractiveOutput
import org.jetbrains.qodana.staticAnalysis.inspections.runner.splitProgressText

class QodanaProgressIndicator(
  private val messageReporter: QodanaMessageReporter
) : ProgressIndicatorBase(), ProgressIndicatorWithDelayedPresentation {
  private var myLastPercent = -1

  init {
    text = ""
  }

  override fun setText(text: String?) {
    if (text == null) {
      return
    }
    val percent = (fraction * 100).toInt()
    if (isInteractiveOutput() || !isIndeterminate && fraction > 0 && myLastPercent != percent) {
      val (prefix, file) = splitProgressText(text)
      val msg = if (isInteractiveOutput() && file != null) "$prefix $percent% [$file]" else "$prefix $percent%"
      messageReporter.reportMessage(2, msg)
    }
    myLastPercent = percent
    return
  }

  override fun setDelayInMillis(delayInMillis: Int) {
  }
}
