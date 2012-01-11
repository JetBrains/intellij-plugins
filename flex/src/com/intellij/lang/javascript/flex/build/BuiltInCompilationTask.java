package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BuiltInCompilationTask extends FlexCompilationTask {

  private final BuiltInFlexCompilerHandler myBuiltInFlexCompilerHandler;
  private BuiltInFlexCompilerHandler.Listener myListener;

  public BuiltInCompilationTask(final @NotNull Module module,
                                final @Nullable FlexFacet flexFacet,
                                final @NotNull FlexBuildConfiguration oldConfig) {
    super(module.getName() + (flexFacet == null ? "" : " (" + flexFacet.getName() + ")"), module, flexFacet, oldConfig, null, null);
    myBuiltInFlexCompilerHandler = FlexCompilerHandler.getInstance(module.getProject()).getBuiltInFlexCompilerHandler();
  }

  public BuiltInCompilationTask(final @NotNull Module module,
                                final @NotNull FlexIdeBuildConfiguration flexIdeConfig,
                                final @NotNull Collection<FlexIdeBuildConfiguration> configDependencies) {
    super(flexIdeConfig.getName() + " (" + module.getName() + ")", module, null, null, flexIdeConfig, configDependencies);
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
    if (myOldConfig != null) {
      final boolean swf = myOldConfig.OUTPUT_TYPE.equals(FlexBuildConfiguration.APPLICATION);
      final List<String> compilerCommand = Collections.singletonList(swf ? "mxmlc" : "compc");
      return FlexCompilationUtils.buildCommand(compilerCommand, getConfigFiles(), myModule, myOldConfig);
    }
    else {
      assert myFlexIdeConfig != null;
      final boolean swf = myFlexIdeConfig.getOutputType() != OutputType.Library;
      final List<String> compilerCommand = Collections.singletonList(swf ? "mxmlc" : "compc");
      return FlexCompilationUtils.buildCommand(compilerCommand, getConfigFiles(), myModule, myFlexIdeConfig);
    }
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

  public static class BuiltInCSSCompilationTask extends BuiltInCompilationTask implements CssCompilationTask {
    private final String myCssFilePath;

    public BuiltInCSSCompilationTask(final Module module,
                                     final @Nullable FlexFacet flexFacet,
                                     final FlexBuildConfiguration config,
                                     final String cssFilePath) {
      super(module, flexFacet, FlexCompilationUtils.createCssConfig(config, cssFilePath));
      myCssFilePath = FileUtil.toSystemIndependentName(cssFilePath);
    }

    protected List<VirtualFile> createConfigFiles() throws IOException {
      return FlexCompilationUtils.getConfigFiles(myOldConfig, myModule, myFlexFacet, myCssFilePath);
    }
  }
}

