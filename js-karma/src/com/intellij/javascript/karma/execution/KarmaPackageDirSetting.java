package com.intellij.javascript.karma.execution;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "JsKarmaPackageDirSetting",
  storages = {
    @Storage(file = StoragePathMacros.WORKSPACE_FILE)
  }
)
// Remove in IDEA 16
public class KarmaPackageDirSetting implements PersistentStateComponent<Element> {

  private String myPackageDir;

  @Nullable
  @Override
  public Element getState() {
    return null;
  }

  @Override
  public void loadState(Element state) {
    if (state != null) {
      Element child = state.getChild("data");
      if (child != null) {
        myPackageDir = child.getText();
      }
    }
  }

  @NotNull
  public static KarmaPackageDirSetting getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, KarmaPackageDirSetting.class);
  }

  @Nullable
  public String getPackageDir() {
    String result = myPackageDir;
    myPackageDir = null;
    return result;
  }
}
