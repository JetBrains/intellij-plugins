package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public abstract class FlexCompilationTask {

  protected final Module myModule;
  private final String myPresentableName;
  protected final @Nullable FlexFacet myFlexFacet;
  // one of the following configs is not null
  protected final @Nullable FlexBuildConfiguration myOldConfig;
  protected final @Nullable FlexIdeBuildConfiguration myFlexIdeConfig;

  private List<VirtualFile> myConfigFiles;

  protected boolean myFinished;
  protected boolean myCompilationFailed;

  protected FlexCompilationTask(final @NotNull String presentableName,
                                final @NotNull Module module,
                                final @Nullable FlexFacet flexFacet,
                                final @Nullable FlexBuildConfiguration oldConfig,
                                final @Nullable FlexIdeBuildConfiguration flexIdeConfig) {
    myModule = module;
    myPresentableName = presentableName;
    myFlexFacet = flexFacet;
    myOldConfig = oldConfig;
    myFlexIdeConfig = flexIdeConfig;
  }

  public void start(final FlexCompilationManager compilationManager) {
    try {
      myConfigFiles = createConfigFiles();

      if (!compilationManager.isMake()) {
        final VirtualFile configFile = myConfigFiles.get(myConfigFiles.size() - 1);
        final String outputFilePath = FlexUtils.findXMLElement(configFile.getInputStream(), "<flex-config><output>");
        FlexCompilationUtils.deleteCacheForFile(outputFilePath);
      }

      if (myOldConfig != null) {
        if (!myOldConfig.USE_CUSTOM_CONFIG_FILE) {
          FlexCompilationUtils.ensureOutputFileWritable(myModule.getProject(), myOldConfig.getOutputFileFullPath());
        }
      }
      else {
        assert myFlexIdeConfig != null;
        FlexCompilationUtils.ensureOutputFileWritable(myModule.getProject(), myFlexIdeConfig.getOutputFilePath());
      }

      doStart(compilationManager);
    }
    catch (IOException e) {
      compilationManager.addMessage(this, CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
      myCompilationFailed = true;
      cancel();
    }
  }

  protected List<VirtualFile> createConfigFiles() throws IOException {
    if (myOldConfig != null) {
      return FlexCompilationUtils.getConfigFiles(myOldConfig, myModule, myFlexFacet, null);
    }
    else {
      return Collections.singletonList(CompilerConfigGenerator.getOrCreateConfigFile(myModule, myFlexIdeConfig));
    }
  }

  protected abstract void doStart(final FlexCompilationManager compilationManager) throws IOException;

  public void cancel() {
    doCancel();
    myFinished = true;
  }

  protected abstract void doCancel();

  public boolean isFinished() {
    return myFinished;
  }

  public boolean isCompilationFailed() {
    return myCompilationFailed;
  }

  public String getPresentableName() {
    return myPresentableName;
  }

  public Module getModule() {
    return myModule;
  }

  public List<VirtualFile> getConfigFiles() {
    return myConfigFiles;
  }

  public boolean useCache() {
    return myOldConfig != null && myOldConfig.getType() == FlexBuildConfiguration.Type.Default;
  }

  @Nullable
  public FlexIdeBuildConfiguration getFlexIdeConfig() {
    return myFlexIdeConfig;
  }
}
