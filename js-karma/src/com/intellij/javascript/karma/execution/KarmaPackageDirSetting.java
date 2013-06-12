package com.intellij.javascript.karma.execution;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jdom.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "JsKarmaPackageDirSetting",
  storages = {
    @Storage( file = StoragePathMacros.WORKSPACE_FILE)
  }
)
public class KarmaPackageDirSetting implements PersistentStateComponent<Element> {

  private String myPackageDir;

  @Nullable
  @Override
  public Element getState() {
    if (StringUtil.isEmpty(myPackageDir)) {
      return null;
    }
    Element element = new Element("karma-settings");
    Element child = new Element("data");
    child.addContent(new Text(myPackageDir));
    element.addContent(child);
    return element;
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
    return myPackageDir;
  }

  public void setPackageDir(@NotNull String packageDir) {
    myPackageDir = packageDir;
  }

}
