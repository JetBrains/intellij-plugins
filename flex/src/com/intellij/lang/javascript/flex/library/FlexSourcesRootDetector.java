package com.intellij.lang.javascript.flex.library;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.roots.OrderRootType;

/**
 * User: ksafonov
 */
class FlexSourcesRootDetector extends AsRootDetectorBase {

  public FlexSourcesRootDetector() {
    super(OrderRootType.SOURCES, FlexBundle.message("sources.root.detector.name"), true);
  }

}
