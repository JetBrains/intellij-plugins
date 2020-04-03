package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service
public final class PlatformioService {
  private State myState = State.NONE;

  public PlatformioService() {
  }

  public @NotNull State getState() {
    return myState;
  }

  public void setState(@NotNull State state) {
    myState = state;
  }

  public void enable(boolean b) {
    myState = b ? State.OK : State.NONE;
  }

  public static State getState(@Nullable Project project) {
    if (project == null || project.isDefault()) return State.NONE;
    PlatformioService platformioService = ServiceManager.getServiceIfCreated(project, PlatformioService.class);
    return platformioService == null ? State.NONE : platformioService.getState();
  }

  public enum State {
    NONE,
    BROKEN,
    OUTDATED,
    OK
  }
}
