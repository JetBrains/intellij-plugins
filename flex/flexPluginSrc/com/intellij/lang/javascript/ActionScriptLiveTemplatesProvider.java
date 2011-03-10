package com.intellij.lang.javascript;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;
import org.jetbrains.annotations.NonNls;

public class ActionScriptLiveTemplatesProvider implements DefaultLiveTemplatesProvider {

  private static final @NonNls String[] DEFAULT_TEMPLATES = new String[]{
    "/liveTemplates/actionscript_iterations",
    "/liveTemplates/actionscript_miscellaneous"
  };

  public String[] getDefaultLiveTemplateFiles() {
    return DEFAULT_TEMPLATES;
  }

  @Override
  public String[] getHiddenLiveTemplateFiles() {
    return null;
  }

}
