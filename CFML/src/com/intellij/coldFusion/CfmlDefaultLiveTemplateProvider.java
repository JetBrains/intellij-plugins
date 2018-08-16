// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;
import org.jetbrains.annotations.NonNls;

public class CfmlDefaultLiveTemplateProvider implements DefaultLiveTemplatesProvider {

  private static final @NonNls String[] DEFAULT_TEMPLATES = new String[]{
    "/liveTemplates/cfml_default"
  };

  @Override
  public String[] getDefaultLiveTemplateFiles() {
    return DEFAULT_TEMPLATES;
  }

  @Override
  public String[] getHiddenLiveTemplateFiles() {
    return null;
  }
}
