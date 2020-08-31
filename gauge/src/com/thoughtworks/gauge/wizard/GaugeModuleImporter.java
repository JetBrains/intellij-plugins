package com.thoughtworks.gauge.wizard;

import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

public interface GaugeModuleImporter {
  String getId();

  void importModule(@NotNull Module module, GaugeTemplate selectedTemplate);
}
