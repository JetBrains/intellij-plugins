package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class FlexCompilationTask {

  protected final Module myModule;
  private final String myPresentableName;
  protected final FlexIdeBuildConfiguration myBC;
  protected final Collection<FlexIdeBuildConfiguration> myDependencies;

  private List<VirtualFile> myConfigFiles;

  protected boolean myFinished;
  protected boolean myCompilationFailed;

  protected FlexCompilationTask(final Module module,
                                final FlexIdeBuildConfiguration bc,
                                final Collection<FlexIdeBuildConfiguration> dependencies) {
    myModule = module;
    myBC = bc;
    myDependencies = dependencies;
    myPresentableName = bc.getName() + " (" + module.getName() + ")";
  }

  public void start(final FlexCompilationManager compilationManager) {
    try {
      myConfigFiles = createConfigFiles();

      if (!compilationManager.isMake()) {
        final VirtualFile configFile = myConfigFiles.get(myConfigFiles.size() - 1);
        final String outputFilePath = FlexUtils.findXMLElement(configFile.getInputStream(), "<flex-config><output>");
        FlexCompilationUtils.deleteCacheForFile(outputFilePath);
      }

      FlexCompilationUtils.ensureOutputFileWritable(myModule.getProject(), myBC.getOutputFilePath());
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
    if (!additionalConfigFilePath.isEmpty()) {
      final VirtualFile additionalConfigFile = LocalFileSystem.getInstance().findFileByPath(additionalConfigFilePath);
      if (additionalConfigFile == null) {
        throw new IOException(FlexBundle.message("additional.config.file.not.found", additionalConfigFilePath, myBC.getName(),
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

  public boolean useCache() {
    return false;
  }

  @Nullable
  public FlexIdeBuildConfiguration getBC() {
    return myBC;
  }

  @Nullable
  public Collection<FlexIdeBuildConfiguration> getDependencies() {
    return myDependencies;
  }
}
