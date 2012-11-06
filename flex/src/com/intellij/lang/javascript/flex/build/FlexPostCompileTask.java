package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.CompilerWorkspaceConfiguration;
import com.intellij.flex.FlexCommonBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;
import com.intellij.openapi.compiler.CompilerMessage;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class FlexPostCompileTask implements CompileTask {

  public boolean execute(final CompileContext context) {
    if (!CompilerWorkspaceConfiguration.getInstance(context.getProject()).useOutOfProcessBuild()) {
      return true;
    }


    try {
      final CompilerMessage[] infoMessages = context.getMessages(CompilerMessageCategory.INFORMATION);

      for (final Pair<Module, FlexBuildConfiguration> moduleAndBC : FlexCompiler.getModulesAndBCsToCompile(context.getCompileScope())) {
        final String prefix = "[" + FlexCompilationTask.getPresentableName(moduleAndBC.first, moduleAndBC.second) + "]";

        final List<String> ownMessages = filterOwnMessages(infoMessages, prefix);

        if (ownMessages.size() > 0 &&
            ownMessages.get(ownMessages.size() - 1).endsWith(FlexCommonBundle.message("compilation.successful"))) {
          try {
            FlexCompilationUtils.performPostCompileActions(moduleAndBC.first, moduleAndBC.second, ownMessages);
          }
          catch (FlexCompilerException e) {
            context.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), e.getUrl(), e.getLine(), e.getColumn());
          }
        }
      }
    }
    catch (ConfigurationException ignore) {/* already reported */}

    return true;
  }

  private static List<String> filterOwnMessages(final CompilerMessage[] messages, final String prefix) {
    final ArrayList<String> result = new ArrayList<String>();

    for (CompilerMessage message : messages) {
      if (message.getMessage().startsWith(prefix)) {
        result.add(message.getMessage());
      }
    }

    return result;
  }
}
