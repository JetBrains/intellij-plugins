// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.folding;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.SettingsCategory;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@State(name = "DartCodeFoldingSettings", storages = @Storage("editor.xml"), category = SettingsCategory.CODE)
public class DartCodeFoldingSettings implements PersistentStateComponent<DartCodeFoldingSettings> {
  private boolean myCollapseParts = true;
  private boolean myCollapseGenericParams;

  public static DartCodeFoldingSettings getInstance() {
    return ApplicationManager.getApplication().getService(DartCodeFoldingSettings.class);
  }

  @Override
  public DartCodeFoldingSettings getState() {
    return this;
  }

  @Override
  public void loadState(final @NotNull DartCodeFoldingSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public boolean isCollapseGenericParameters() {
    return myCollapseGenericParams;
  }

  public void setCollapseGenericParameters(final boolean collapseGenericParams) {
    myCollapseGenericParams = collapseGenericParams;
  }

  public boolean isCollapseParts() {
    return myCollapseParts;
  }

  public void setCollapseParts(final boolean collapseParts) {
    myCollapseParts = collapseParts;
  }
}
