package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.JSLinterPackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "PrettierConfiguration")
public class PrettierConfiguration implements PersistentStateComponent<PrettierConfiguration.State> {
  @NotNull
  private final JSLinterPackage myPackage;

  public PrettierConfiguration(@NotNull Project project) {
    myPackage = new JSLinterPackage(project, "prettier");
  }

  @NotNull
  public static PrettierConfiguration getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, PrettierConfiguration.class);
  }

  @NotNull
  public NodeJsInterpreterRef getOrDetectInterpreterRef() {
    myPackage.readOrDetect();
    return getInterpreterRef();
  }

  @NotNull
  public NodeJsInterpreterRef getInterpreterRef() {
    return myPackage.getInterpreter();
  }

  @Nullable
  public NodePackage getPackage() {
    return new NodePackage(myPackage.getPackagePath());
  }

  @Nullable
  public NodePackage getOrDetectNodePackage() {
    myPackage.readOrDetect();
    return getPackage();
  }

  @Nullable
  @Override
  public PrettierConfiguration.State getState() {
    return null;
  }

  public void update(@Nullable NodeJsInterpreter nodeInterpreter, @Nullable NodePackage nodePackage) {
    myPackage.force(nodeInterpreter != null ? nodeInterpreter.toRef() : null,
                    nodePackage != null ? nodePackage.getSystemDependentPath() : null);
  }

  @Override
  public void loadState(@NotNull PrettierConfiguration.State state) {
    myPackage.readOrDetect();
  }

  public static class State {
  }
}
