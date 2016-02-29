package com.jetbrains.lang.dart.folding;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@State(name = "DartCodeFoldingSettings", storages = @Storage("editor.codeinsight.xml"))
public class DartCodeFoldingSettings implements PersistentStateComponent<DartCodeFoldingSettings>, ExportableComponent {
  private boolean myCollapseParts = true;
  private boolean myCollapseGenericParams = false;

  public static DartCodeFoldingSettings getInstance() {
    return ServiceManager.getService(DartCodeFoldingSettings.class);
  }

  @Override
  @NotNull
  public File[] getExportFiles() {
    return new File[]{PathManager.getOptionsFile("editor.codeinsight")};
  }

  @Override
  @NotNull
  public String getPresentableName() {
    return IdeBundle.message("code.folding.settings");
  }

  @Override
  public DartCodeFoldingSettings getState() {
    return this;
  }

  @Override
  public void loadState(final DartCodeFoldingSettings state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  // property name must be equal to checkBox() argument in DartCodeFoldingOptionsProvider
  public boolean isCollapseGenericParameters() {
    return myCollapseGenericParams;
  }

  public void setCollapseGenericParameters(final boolean collapseGenericParams) {
    myCollapseGenericParams = collapseGenericParams;
  }

  // property name must be equal to checkBox() argument in DartCodeFoldingOptionsProvider
  public boolean isCollapseParts() {
    return myCollapseParts;
  }

  public void setCollapseParts(final boolean collapseParts) {
    myCollapseParts = collapseParts;
  }
}
