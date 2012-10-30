package com.intellij.lang.javascript.flex.build;

import com.intellij.flex.FlexCommonBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class FlexCompilationTask {

  protected final Module myModule;
  private final String myPresentableName;
  protected final FlexBuildConfiguration myBC;
  protected final Collection<FlexBuildConfiguration> myDependencies;

  private List<VirtualFile> myConfigFiles;

  protected boolean myFinished;
  protected boolean myCompilationFailed;

  protected FlexCompilationTask(final Module module,
                                final FlexBuildConfiguration bc,
                                final Collection<FlexBuildConfiguration> dependencies) {
    myModule = module;
    myBC = bc;
    myDependencies = dependencies;

    String postfix = bc.isTempBCForCompilation() ? " - " + BCUtils.getBCSpecifier(bc) : "";
    if (!bc.getName().equals(module.getName())) postfix += " (module " + module.getName() + ")";
    myPresentableName = bc.getName() + postfix;
  }

  public void start(final FlexCompilationManager compilationManager) {
    try {
      myConfigFiles = createConfigFiles();
      final String outputFilePath = myBC.getActualOutputFilePath();

      if (compilationManager.isRebuild()) {
        FlexCompilationUtils.deleteCacheForFile(outputFilePath);
      }

      FlexCompilationUtils.ensureOutputFileWritable(myModule.getProject(), outputFilePath);
      doStart(compilationManager);
    }
    catch (IOException e) {
      compilationManager.addMessage(this, CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
      myCompilationFailed = true;
      cancel();
    }
  }

  protected List<VirtualFile> createConfigFiles() throws IOException {
    final ArrayList<VirtualFile> configFiles = new ArrayList<VirtualFile>(2);
    configFiles.add(CompilerConfigGenerator.getOrCreateConfigFile(myModule, myBC));

    final String additionalConfigFilePath = myBC.getCompilerOptions().getAdditionalConfigFilePath();
    if (!myBC.isTempBCForCompilation() && !additionalConfigFilePath.isEmpty()) {
      final VirtualFile additionalConfigFile = LocalFileSystem.getInstance().findFileByPath(additionalConfigFilePath);
      if (additionalConfigFile == null) {
        throw new IOException(
          FlexCommonBundle.message("additional.config.file.not.found.for.bc.0.of.module.1", additionalConfigFilePath, myBC.getName(),
                                   myModule.getName()));
      }
      configFiles.add(additionalConfigFile);
    }

    return configFiles;
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

  public FlexBuildConfiguration getBC() {
    return myBC;
  }

  public Collection<FlexBuildConfiguration> getDependencies() {
    return myDependencies;
  }
}
