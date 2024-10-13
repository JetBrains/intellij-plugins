package org.jetbrains.qodana.ui.problemsView.tree.ui

import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.FontUtil
import org.jetbrains.qodana.QodanaBundle

fun PresentationData.appendProblemsCount(problemsCount: Int) {
  appendGrayedText(QodanaBundle.message("qodana.problem.count", problemsCount))
}

fun PresentationData.appendGrayedText(@NlsContexts.Label text: String) {
  appendText(text, SimpleTextAttributes.GRAYED_ATTRIBUTES)
}

fun PresentationData.appendText(@NlsContexts.Label text: String, attributes: SimpleTextAttributes) {
  if (text.isEmpty()) return
  addText("${FontUtil.spaceAndThinSpace()}$text", attributes)
}