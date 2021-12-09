package com.thoughtworks.gauge.wizard;

import com.intellij.openapi.util.NlsSafe;

public final class GaugeTemplate {
  public final @NlsSafe String title;
  public final boolean init;
  public final String templateId;
  public final String importerId;

  GaugeTemplate(String title, boolean init, String templateId, String importerId) {
    this.title = title;
    this.init = init;
    this.templateId = templateId;
    this.importerId = importerId;
  }
}
