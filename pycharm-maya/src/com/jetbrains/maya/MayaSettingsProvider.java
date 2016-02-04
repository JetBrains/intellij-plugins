package com.jetbrains.maya;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;

/**
 * @author traff
 */
@State(
  name = "MayaSettingsProvider",
  storages = {
    @Storage(StoragePathMacros.WORKSPACE_FILE)
  }
)
public class MayaSettingsProvider implements PersistentStateComponent<MayaSettingsProvider.State> {
  private State myState = new State();

  public void setPort(int port) {
    myState.myPort = port;
  }

  public int getPort() {
    return myState.myPort;
  }

  public static MayaSettingsProvider getInstance(Project project) {
    return ServiceManager.getService(project, MayaSettingsProvider.class);
  }

  @Override
  public State getState() {
    return myState;
  }

  @Override
  public void loadState(State state) {
    myState.myPort = state.myPort;
  }

  public static class State {
    public int myPort;
  }
}

