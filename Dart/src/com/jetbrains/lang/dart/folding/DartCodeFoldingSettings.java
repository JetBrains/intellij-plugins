package com.jetbrains.lang.dart.folding;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@State(
  name = "DartCodeFoldingSettings",
  storages= {
    @Storage(
      file = StoragePathMacros.APP_CONFIG + "/editor.codeinsight.xml"
    )}
)
public class DartCodeFoldingSettings
  implements PersistentStateComponent<DartCodeFoldingSettings>, ExportableComponent {


  @SuppressWarnings({"WeakerAccess"}) public boolean DART_COLLAPSE_GENERIC_PARAMETERS = true;

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

  public boolean isCollapseGenericParameters() {
    return DART_COLLAPSE_GENERIC_PARAMETERS;
  }

  public void setCollapseGenericParameters(final boolean value) {
    DART_COLLAPSE_GENERIC_PARAMETERS = value;
  }
}
