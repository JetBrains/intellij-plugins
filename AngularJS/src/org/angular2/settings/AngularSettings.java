package org.angular2.settings;

import com.intellij.lang.typescript.compiler.ui.TypeScriptServerServiceSettings;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@State(name = "AngularJSSettings", storages = @Storage("angular-settings.xml"))
public class AngularSettings implements
                             TypeScriptServerServiceSettings,
                             PersistentStateComponent<AngularSettings.Settings> {

  private final SimpleModificationTracker myModificationTracker = new SimpleModificationTracker();  
  
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
  public void loadState(@NotNull AngularSettings.Settings state) {
    if (myState == null || myState.useService != state.useService) {
      myModificationTracker.incModificationCount();
    }
    myState = state;
  }

  @Override
  public boolean isUseService() {
    return myState.useService;
  }

  @Override
  public void setUseService(boolean useService) {
    if (myState.useService != useService) {
      myModificationTracker.incModificationCount();
    }
    myState.useService = useService;
  }

  @Override
  public String getTitle() {
    return "Angular Language Service";
  }

  @NotNull
  @Override
  public ModificationTracker getModificationTracker() {
    return myModificationTracker;
  }
}
