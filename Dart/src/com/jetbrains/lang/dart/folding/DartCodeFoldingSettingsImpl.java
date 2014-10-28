package com.jetbrains.lang.dart.folding;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@State(
  name = "DartCodeFoldingSettings",
  storages = {
    @Storage(
      file = StoragePathMacros.APP_CONFIG + "/com.jetbrains.lang.dart.xml"
    )}
)
public class DartCodeFoldingSettingsImpl extends DartCodeFoldingSettingsBase
  implements PersistentStateComponent<DartCodeFoldingSettingsImpl>,
             ExportableComponent {


  @Override
  @NotNull
  public File[] getExportFiles() {
    return new File[]{PathManager.getOptionsFile("com.jetbrains.lang.dart")};
  }

  @Override
  @NotNull
  public String getPresentableName() {
    return IdeBundle.message("code.folding.settings");
  }

  @Override
  public DartCodeFoldingSettingsImpl getState() {
    return this;
  }

  @Override
  public void loadState(final DartCodeFoldingSettingsImpl state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
