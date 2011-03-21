package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class BuiltInCompilationTask implements FlexCompilationTask {

  private final Module myModule;
  @Nullable private final FlexFacet myFlexFacet;
  private final FlexBuildConfiguration myConfig;
  private final String myPresentableName;

  private final BuiltInFlexCompilerHandler myBuiltInFlexCompilerHandler;
  private boolean myFinished;
  private boolean myCompilationFailed;

  private List<VirtualFile> myConfigFiles;
  private BuiltInFlexCompilerHandler.Listener myListener;

  public BuiltInCompilationTask(final Module module, final @Nullable FlexFacet flexFacet, final FlexBuildConfiguration config) {
    myModule = module;
    myFlexFacet = flexFacet;
    myConfig = config;
    myPresentableName = module.getName() + (flexFacet == null ? "" : " (" + flexFacet.getName() + ")");
    myFinished = false;
    myCompilationFailed = false;
    myBuiltInFlexCompilerHandler = FlexCompilerHandler.getInstance(module.getProject()).getBuiltInFlexCompilerHandler();
  }

  public void start(final FlexCompilationManager compilationManager) {
    try {
      myConfigFiles = createConfigFiles();

      if (!compilationManager.isMake()) {
        final VirtualFile configFile = myConfigFiles.get(myConfigFiles.size() - 1);
        final String outputFilePath = FlexUtils.findXMLElement(configFile.getInputStream(), "<flex-config><output>");
        FlexCompilationUtils.deleteCacheForFile(outputFilePath);
      }

      final List<String> compilerCommand =
        Collections.singletonList(myConfig.OUTPUT_TYPE.equals(FlexBuildConfiguration.APPLICATION) ? "mxmlc" : "compc");
      final List<String> command = FlexCompilationUtils.buildCommand(compilerCommand, myConfigFiles, myModule, myConfig);
      final String plainCommand = StringUtil.join(command, new Function<String, String>() {
        public String fun(final String s) {
          return s.indexOf(' ') >= 0 && !(s.startsWith("\"") && s.endsWith("\"")) ? '\"' + s + '\"' : s;
        }
      }, " ");

      compilationManager.addMessage(this, CompilerMessageCategory.INFORMATION, plainCommand, null, -1, -1);

      myListener = createListener(compilationManager);
      myBuiltInFlexCompilerHandler.sendCompilationCommand(plainCommand, myListener);
    }
    catch (IOException e) {
      compilationManager.addMessage(this, CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
      myCompilationFailed = true;
    }
  }

  protected List<VirtualFile> createConfigFiles() throws IOException {
    return FlexCompilationUtils.getConfigFiles(myConfig, myModule, myFlexFacet, null);
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

  public void cancel() {
    if (myListener != null) {
      myBuiltInFlexCompilerHandler.removeListener(myListener);
    }
    myFinished = true;
  }

  public boolean isFinished() {
    return myFinished;
  }

  public boolean isCompilationFailed() {
    return myCompilationFailed;
  }

  public String getPresentableName() {
    return myPresentableName;
  }

  public FlexBuildConfiguration getConfig() {
    return myConfig;
  }

  public Module getModule() {
    return myModule;
  }

  public List<VirtualFile> getConfigFiles() {
    return myConfigFiles;
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
      return FlexCompilationUtils.getConfigFiles(super.myConfig, super.myModule, super.myFlexFacet, myCssFilePath);
    }
  }
}

