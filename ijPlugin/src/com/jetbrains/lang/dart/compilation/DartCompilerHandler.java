package com.jetbrains.lang.dart.compilation;

import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DartCompilerHandler extends AbstractProjectComponent {
  public DartCompilerHandler(Project project) {
    super(project);
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "DartCompilerHandler";
  }

  public void projectOpened() {
    CompilerManager compilerManager = CompilerManager.getInstance(myProject);
    if (compilerManager != null) {
      compilerManager.addBeforeTask(new DartCompiler());
    }
  }
}
