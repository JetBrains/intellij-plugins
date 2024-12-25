package com.intellij.javascript.bower;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BowerSettings {
  private final Project myProject;
  private final NodePackage myBowerPackage;
  private final String myBowerJsonPath;

  private BowerSettings(@NotNull Builder builder) {
    myProject = builder.myProject;
    myBowerPackage = builder.myBowerPackage;
    myBowerJsonPath = FileUtil.toSystemIndependentName(builder.myBowerJsonPath);
  }

  public @Nullable NodeJsInterpreter getInterpreter() {
    return NodeJsInterpreterManager.getInstance(myProject).getInterpreter();
  }

  public @NotNull NodePackage getBowerPackage() {
    return myBowerPackage;
  }

  public @NotNull String getBowerJsonPath() {
    return myBowerJsonPath;
  }

  public @NotNull Builder createBuilder() {
    Builder builder = new Builder(myProject);
    builder.setBowerPackage(myBowerPackage);
    builder.setBowerJsonPath(myBowerJsonPath);
    return builder;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BowerSettings settings = (BowerSettings)o;

    return myBowerPackage.equals(settings.myBowerPackage)
           && myBowerJsonPath.equals(settings.myBowerJsonPath);
  }

  @Override
  public int hashCode() {
    int result = myBowerPackage.hashCode();
    result = 31 * result + myBowerJsonPath.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "bowerPackage='" + myBowerPackage + '\'' +
           ", bower.json='" + myBowerJsonPath + '\'';
  }

  public static class Builder {
    private final Project myProject;
    private NodePackage myBowerPackage = new NodePackage("");
    private String myBowerJsonPath = "";

    public Builder(@NotNull Project project) {
      myProject = project;
    }

    public @NotNull Builder setBowerPackage(@NotNull NodePackage bowerPackage) {
      myBowerPackage = bowerPackage;
      return this;
    }

    public @NotNull Builder setBowerJsonPath(@NotNull String bowerJsonPath) {
      myBowerJsonPath = bowerJsonPath;
      return this;
    }

    public @NotNull BowerSettings build() {
      return new BowerSettings(this);
    }
  }
}
