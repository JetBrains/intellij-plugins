package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MxmlcCompcCompilationTask implements FlexCompilationTask {

  private final Module myModule;
  @Nullable private final FlexFacet myFlexFacet;
  private final FlexBuildConfiguration myConfig;
  private final String myPresentableName;

  private Process myProcess;
  private boolean myFinished;
  private boolean myCompilationFailed;

  private List<VirtualFile> myConfigFiles;

  public MxmlcCompcCompilationTask(final Module module, final @Nullable FlexFacet flexFacet, final FlexBuildConfiguration config) {
    myModule = module;
    myFlexFacet = flexFacet;
    myConfig = config;
    myPresentableName = module.getName() + (flexFacet == null ? "" : " (" + flexFacet.getName() + ")");
    myFinished = false;
    myCompilationFailed = false;
  }

  public void start(final FlexCompilationManager compilationManager) {
    try {
      myConfigFiles = createConfigFiles();

      if (!compilationManager.isMake()) {
        final VirtualFile configFile = myConfigFiles.get(myConfigFiles.size() - 1);
        final String outputFilePath = FlexUtils.findXMLElement(configFile.getInputStream(), "<flex-config><output>");
        FlexCompilationUtils.deleteCacheForFile(outputFilePath);
      }

      final List<String> compilerCommand = FlexCompilationUtils
        .getMxmlcCompcCommand(myModule.getProject(), FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(myModule),
                              myConfig.OUTPUT_TYPE.equals(FlexBuildConfiguration.APPLICATION));
      final List<String> command = FlexCompilationUtils.buildCommand(compilerCommand, myConfigFiles, myModule, myConfig);
      final ProcessBuilder processBuilder = new ProcessBuilder(command);
      processBuilder.redirectErrorStream(true);
      processBuilder.directory(new File(FlexUtils.getFlexCompilerWorkDirPath(myModule.getProject(), null)));

      compilationManager.addMessage(this, CompilerMessageCategory.INFORMATION, StringUtil.join(command, " "), null, -1, -1);

      myProcess = processBuilder.start();
      readInputStream(compilationManager);
    }
    catch (IOException e) {
      compilationManager.addMessage(this, CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
      myCompilationFailed = true;
      cancel();
    }
  }

  protected List<VirtualFile> createConfigFiles() throws IOException {
    return FlexCompilationUtils.getConfigFiles(myConfig, myModule, myFlexFacet, null);
  }

  public void cancel() {
    if (myProcess != null) {
      myProcess.destroy();
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
      super(module, flexFacet, FlexCompilationUtils.createCssConfig(config));
      myCssFilePath = FileUtil.toSystemIndependentName(cssFilePath);
    }

    protected List<VirtualFile> createConfigFiles() throws IOException {
      return FlexCompilationUtils.getConfigFiles(super.myConfig, super.myModule, super.myFlexFacet, myCssFilePath);
    }
  }
}
