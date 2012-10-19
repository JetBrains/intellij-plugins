package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.CompilerWorkspaceConfiguration;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;
import com.intellij.openapi.compiler.FileProcessingCompiler;

public class FlexCompileTask implements CompileTask {

  public boolean execute(final CompileContext context) {
    if (!CompilerWorkspaceConfiguration.getInstance(context.getProject()).useOutOfProcessBuild()) {
      return true;
    }

    final FlexCompiler flexCompiler = FlexCompiler.getInstance(context.getProject());
    final FileProcessingCompiler.ProcessingItem[] items = flexCompiler.getProcessingItems(context);
    if (items.length > 0) {
      flexCompiler.process(context, items);
    }

    return true;
  }
}
