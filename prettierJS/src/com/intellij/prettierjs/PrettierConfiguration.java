package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.JSLinterPackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrettierConfiguration {
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
    detectPackage();
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
    detectPackage();
    return getPackage();
  }

  public void update(@Nullable NodeJsInterpreter nodeInterpreter, @Nullable NodePackage nodePackage) {
    myPackage.force(nodeInterpreter != null ? nodeInterpreter.toRef() : null,
                    nodePackage != null ? nodePackage.getSystemDependentPath() : null);
  }

  public void detectPackage() {
    myPackage.readOrDetect();
  }
}
