package com.intellij.aws.cloudformation;

import com.intellij.diagnostic.ITNReporter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;

public class CloudFormationReportSubmitter extends ITNReporter {
  @Override
  public boolean showErrorInRelease(IdeaLoggingEvent event) {
    return true;
  }
}
