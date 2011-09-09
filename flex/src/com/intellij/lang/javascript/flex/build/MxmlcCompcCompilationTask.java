package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

public class MxmlcCompcCompilationTask extends FlexCompilationTask {

  private Process myProcess;

  public MxmlcCompcCompilationTask(final @NotNull Module module,
                                   final @Nullable FlexFacet flexFacet,
                                   final @NotNull FlexBuildConfiguration oldConfig) {
    super(module.getName() + (flexFacet == null ? "" : " (" + flexFacet.getName() + ")"), module, flexFacet, oldConfig, null, null);
  }

  public MxmlcCompcCompilationTask(final @NotNull Module module,
                                   final @NotNull FlexIdeBuildConfiguration flexIdeConfig,
                                   final @NotNull Collection<FlexIdeBuildConfiguration> configDependencies) {
    super(flexIdeConfig.getName() + " (" + module.getName() + ")", module, null, null, flexIdeConfig, configDependencies);
  }

  protected void doStart(final FlexCompilationManager compilationManager) throws IOException {
    final boolean swf = myOldConfig != null
                        ? myOldConfig.OUTPUT_TYPE.equals(FlexBuildConfiguration.APPLICATION)
                        : myFlexIdeConfig.getOutputType() != OutputType.Library;
    // todo take correct SDK from myFlexIdeConfig.DEPENDENCIES...
    final Sdk sdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(myModule);

    final List<String> compilerCommand = FlexCompilationUtils.getMxmlcCompcCommand(myModule.getProject(), sdk, swf);

    final List<String> command = myOldConfig != null
                                 ? FlexCompilationUtils.buildCommand(compilerCommand, getConfigFiles(), myModule, myOldConfig)
                                 : FlexCompilationUtils.buildCommand(compilerCommand, getConfigFiles());
    final ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    processBuilder.directory(new File(FlexUtils.getFlexCompilerWorkDirPath(myModule.getProject(), null)));

    compilationManager.addMessage(this, CompilerMessageCategory.INFORMATION, StringUtil.join(command, " "), null, -1, -1);

    myProcess = processBuilder.start();
    readInputStream(compilationManager);
  }

  protected void doCancel() {
    if (myProcess != null) {
      myProcess.destroy();
    }
  }

  private void readInputStream(final FlexCompilationManager compilationManager) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        final InputStreamReader reader = new InputStreamReader(myProcess.getInputStream());
        try {
          char[] buf = new char[2048];
          int read;
          while ((read = reader.read(buf, 0, buf.length)) >= 0) {
            final String output = new String(buf, 0, read);
            final boolean ok = FlexCompilationUtils.handleCompilerOutput(compilationManager, MxmlcCompcCompilationTask.this, output);
            if (!ok) {
              myCompilationFailed = true;
            }
          }
        }
        catch (IOException e) {
          compilationManager.addMessage(MxmlcCompcCompilationTask.this, CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
          myCompilationFailed = true;
        }
        finally {
          cancel();

          try {
            reader.close();
          }
          catch (IOException e) {/*ignore*/}
        }
      }
    });
  }

  public static class MxmlcCompcCssCompilationTask extends MxmlcCompcCompilationTask implements CssCompilationTask {
    private final String myCssFilePath;

    public MxmlcCompcCssCompilationTask(final Module module,
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
