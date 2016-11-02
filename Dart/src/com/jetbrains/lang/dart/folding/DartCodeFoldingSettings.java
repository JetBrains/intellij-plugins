package com.jetbrains.lang.dart.folding;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;

@State(name = "DartCodeFoldingSettings", storages = @Storage("editor.codeinsight.xml"))
public class DartCodeFoldingSettings implements PersistentStateComponent<DartCodeFoldingSettings> {
  private boolean myCollapseParts = true;
  private boolean myCollapseGenericParams;

  public static DartCodeFoldingSettings getInstance() {
    return ServiceManager.getService(DartCodeFoldingSettings.class);
  }

  @Override
  public DartCodeFoldingSettings getState() {
    return this;
  }

  @Override
  public void loadState(final DartCodeFoldingSettings state) {
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
