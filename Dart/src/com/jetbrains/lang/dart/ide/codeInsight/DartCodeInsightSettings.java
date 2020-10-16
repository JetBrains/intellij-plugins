// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.codeInsight;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import static com.intellij.codeInsight.CodeInsightSettings.*;

@State(name = "DartCodeInsightSettings", storages = @Storage("other.xml"))
public class DartCodeInsightSettings implements PersistentStateComponent<DartCodeInsightSettings> {
  public static DartCodeInsightSettings getInstance() {
    return ApplicationManager.getApplication().getService(DartCodeInsightSettings.class);
  }

  public boolean INSERT_DEFAULT_ARG_VALUES = true;

  public boolean SHOW_CLOSING_LABELS = true;

  @MagicConstant(intValues = {YES, NO, ASK})
  public int ADD_IMPORTS_ON_PASTE = ASK;

  @Override
  public DartCodeInsightSettings getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull DartCodeInsightSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
