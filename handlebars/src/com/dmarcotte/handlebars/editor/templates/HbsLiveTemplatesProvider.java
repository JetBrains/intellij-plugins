package com.dmarcotte.handlebars.editor.templates;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;
import org.jetbrains.annotations.Nullable;

public class HbsLiveTemplatesProvider implements DefaultLiveTemplatesProvider {
  @Override
  public String[] getDefaultLiveTemplateFiles() {
    return new String[]{"liveTemplates/Handlebars"};
  }

  @Nullable
  @Override
  public String[] getHiddenLiveTemplateFiles() {
    return null;
  }
}
