package com.thoughtworks.gauge.wizard;

import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

public interface GaugeModuleImporter {
  String MINIMAL_GAUGE_JAVA_VERSION = "0.7.9";

  String getId();

  void importModule(@NotNull Module module, GaugeTemplate selectedTemplate);
}
