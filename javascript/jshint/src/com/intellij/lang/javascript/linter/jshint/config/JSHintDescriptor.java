package com.intellij.lang.javascript.linter.jshint.config;

import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.lang.javascript.linter.jshint.JSHintBundle;
import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.lang.javascript.linter.JSLinterConfigFileUtil;
import com.intellij.lang.javascript.linter.JSLinterConfiguration;
import com.intellij.lang.javascript.linter.JSLinterDescriptor;
import com.intellij.lang.javascript.linter.jshint.JSHintConfiguration;
import com.intellij.lang.javascript.linter.jshint.JSHintState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.util.CommonProcessors;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class JSHintDescriptor extends JSLinterDescriptor {

  @Override
  public @NotNull String getDisplayName() {
    return JSHintBundle.message("settings.javascript.linters.jshint.configurable.name");
  }

  @Override
  public boolean hasConfigFiles(@NotNull Project project) {
    return JSLinterConfigFileUtil.projectHasConfigFiles(project, JSHintConfigFileType.INSTANCE);
  }

  @Override
  public boolean enable(@NotNull Project project, Collection<PackageJsonData> packageJsonFiles) {
    boolean enabled = super.enable(project, packageJsonFiles);
    if (enabled) {
      final CommonProcessors.CollectProcessor<VirtualFile> processor = new CommonProcessors.CollectProcessor<>();
      FileTypeIndex.processFiles(JSHintConfigFileType.INSTANCE, processor, JSLibraryUtil.getContentScopeWithoutLibraries(project));
      processor.getResults().stream().findFirst().ifPresent(file -> {
        final JSHintConfiguration configuration = JSHintConfiguration.getInstance(project);
        final JSHintState state = configuration.getExtendedState().getState();
        final JSHintState newState = new JSHintState.Builder(state)
          .setConfigFileUsed(true)
          .setCustomConfigFileUsed(true)
          .setCustomConfigFilePath(FileUtil.toSystemDependentName(file.getPath()))
          .build();
        configuration.setExtendedState(true, newState);
      });  
    }
    return enabled;
  }

  @Override
  public @NotNull Class<? extends JSLinterConfiguration> getConfigurationClass() {
    return JSHintConfiguration.class;
  }
}
