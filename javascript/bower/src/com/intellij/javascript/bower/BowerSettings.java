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

  @Nullable
  public NodeJsInterpreter getInterpreter() {
    return NodeJsInterpreterManager.getInstance(myProject).getInterpreter();
  }

  @NotNull
  public NodePackage getBowerPackage() {
    return myBowerPackage;
  }

  @NotNull
  public String getBowerJsonPath() {
    return myBowerJsonPath;
  }

  @NotNull
  public Builder createBuilder() {
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

    @NotNull
    public Builder setBowerPackage(@NotNull NodePackage bowerPackage) {
      myBowerPackage = bowerPackage;
      return this;
    }

    @NotNull
    public Builder setBowerJsonPath(@NotNull String bowerJsonPath) {
      myBowerJsonPath = bowerJsonPath;
      return this;
    }

    @NotNull
    public BowerSettings build() {
      return new BowerSettings(this);
    }
  }
}
