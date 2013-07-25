package com.intellij.coldFusion;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;
import org.jetbrains.annotations.NonNls;

public class CfmlDefaultLiveTemplateProvider implements DefaultLiveTemplatesProvider {

  private static final @NonNls String[] DEFAULT_TEMPLATES = new String[]{
    "/liveTemplates/cfml_default"
  };

  public String[] getDefaultLiveTemplateFiles() {
    return DEFAULT_TEMPLATES;
  }

  @Override
  public String[] getHiddenLiveTemplateFiles() {
    return null;
  }
}
