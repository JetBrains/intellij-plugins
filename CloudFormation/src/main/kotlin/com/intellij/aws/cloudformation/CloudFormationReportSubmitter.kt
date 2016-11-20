package com.intellij.aws.cloudformation

import com.intellij.diagnostic.ITNReporter
import com.intellij.openapi.diagnostic.IdeaLoggingEvent

class CloudFormationReportSubmitter : ITNReporter() {
  override fun showErrorInRelease(event: IdeaLoggingEvent): Boolean = true
}
