package com.jetbrains.plugins.meteor.runner.liveEdit;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.javascript.debugger.execution.JavaScriptDebugConfiguration;
import com.intellij.xdebugger.XDebugProcess;
import com.jetbrains.liveEdit.UpdatePolicy;
import com.jetbrains.liveEdit.update.UpdateConfiguration;
import com.jetbrains.liveEdit.update.UpdateConfigurationProvider;
import com.jetbrains.plugins.meteor.MeteorFacade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MeteorClientUpdateConfigurationProvider extends UpdateConfigurationProvider {
  private static final UpdateConfiguration UPDATE_CONFIGURATION = new UpdateConfiguration() {
    @NotNull
    @Override
    public UpdatePolicy getPolicy() {
      return UpdatePolicy.DISABLED;
    }
  };

  @Nullable
  @Override
  public UpdateConfiguration getConfiguration(@NotNull XDebugProcess debugProcess) {
    RunProfile runProfile = debugProcess.getSession().getRunProfile();
    return runProfile != null && isSupported(runProfile) ? UPDATE_CONFIGURATION : null;
  }

  @Override
  public boolean isSupported(@NotNull RunProfile runProfile) {
    return runProfile instanceof JavaScriptDebugConfiguration &&
           MeteorFacade.getInstance().isMeteorProject(((JavaScriptDebugConfiguration)runProfile).getProject());
  }
}
