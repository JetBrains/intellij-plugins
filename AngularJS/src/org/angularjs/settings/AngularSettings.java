package org.angularjs.settings;

import com.intellij.lang.typescript.compiler.ui.TypeScriptServerServiceSettings;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;


@State(name = "AngularJSSettings", storages = @Storage("angular-settings.xml"))
public class AngularSettings implements
                             TypeScriptServerServiceSettings,
                             PersistentStateComponent<AngularSettings.Settings> {

  private Settings myState = new AngularSettings.Settings();

  public static AngularSettings get(Project project) {
    return ServiceManager.getService(project, AngularSettings.class);
  }


  static class Settings {
    public boolean useService = true;
  }

  @Nullable
  @Override
  public AngularSettings.Settings getState() {
    return myState;
  }

  @Override
  public void loadState(AngularSettings.Settings state) {
    myState = state;
  }

  public boolean isUseService() {
    return myState.useService;
  }

  public void setUseService(boolean useService) {
    myState.useService = useService;
  }

  @Override
  public String getTitle() {
    return "Angular Language Service";
  }
}
