package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.lang.javascript.flex.build.FlexCompilerHandler;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;

public class FlexUnitAfterCompileTask implements CompileTask {

  public boolean execute(final CompileContext context) {
    FlexCompilerHandler.deleteTempFlexUnitFiles(context);
    return true;
  }
}
