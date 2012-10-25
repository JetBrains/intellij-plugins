package com.intellij.lang.javascript.flex.build;

import com.intellij.flex.model.bc.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BuiltInCompilationTask extends FlexCompilationTask {

  private final BuiltInFlexCompilerHandler myBuiltInFlexCompilerHandler;
  private BuiltInFlexCompilerHandler.Listener myListener;

  public BuiltInCompilationTask(final @NotNull Module module,
                                final @NotNull FlexBuildConfiguration bc,
                                final @NotNull Collection<FlexBuildConfiguration> dependencies) {
    super(module, bc, dependencies);
    myBuiltInFlexCompilerHandler = FlexCompilerHandler.getInstance(module.getProject()).getBuiltInFlexCompilerHandler();
  }

  protected void doStart(final FlexCompilationManager compilationManager) {
    final String plainCommand = StringUtil.join(buildCommand(), new Function<String, String>() {
      public String fun(final String s) {
        return s.indexOf(' ') >= 0 && !(s.startsWith("\"") && s.endsWith("\"")) ? '\"' + s + '\"' : s;
      }
    }, " ");

    compilationManager.addMessage(this, CompilerMessageCategory.INFORMATION, plainCommand, null, -1, -1);

    myListener = createListener(compilationManager);
    myBuiltInFlexCompilerHandler.sendCompilationCommand(plainCommand, myListener);
  }

  private List<String> buildCommand() {
    final boolean app = myBC.getOutputType() != OutputType.Library;
    final List<String> compilerCommand = Collections.singletonList(app ? "mxmlc" : "compc");
    return FlexCompilationUtils.buildCommand(compilerCommand, getConfigFiles(), myModule, myBC);
  }

  private BuiltInFlexCompilerHandler.Listener createListener(final FlexCompilationManager compilationManager) {
    return new BuiltInFlexCompilerHandler.Listener() {
      public void textAvailable(final String text) {
        final boolean ok = FlexCompilationUtils.handleCompilerOutput(compilationManager, BuiltInCompilationTask.this, text);
        if (!ok) {
          myCompilationFailed = true;
        }
      }

      public void compilationFinished() {
        myFinished = true;
      }
    };
  }

  protected void doCancel() {
    if (myListener != null) {
      myBuiltInFlexCompilerHandler.removeListener(myListener);
    }
  }
}

