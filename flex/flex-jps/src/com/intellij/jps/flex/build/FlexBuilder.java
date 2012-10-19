package com.intellij.jps.flex.build;

import com.intellij.flex.build.FlexBuildTarget;
import com.intellij.flex.build.FlexBuildTargetType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;

import java.io.IOException;
import java.util.Collections;

public class FlexBuilder extends TargetBuilder<BuildRootDescriptor, FlexBuildTarget> {

  private static final @NonNls String BUILDER_NAME = "flex";

  protected FlexBuilder() {
    super(Collections.singletonList(FlexBuildTargetType.INSTANCE));
  }

  public String getName() {
    return BUILDER_NAME;
  }

  public String getDescription() {
    return "Flash Compiler";
  }

  public void build(@NotNull final FlexBuildTarget target,
                    @NotNull final DirtyFilesHolder<BuildRootDescriptor, FlexBuildTarget> holder,
                    @NotNull final BuildOutputConsumer outputConsumer,
                    @NotNull final CompileContext context) throws ProjectBuildException, IOException {
  }

 /* public static void compileBuildConfiguration(final CompileContext context, final JpsFlexBuildConfiguration bc) {
    try {
      final List<File> configFiles = createConfigFiles(bc);
      final String outputFilePath = bc.getActualOutputFilePath();

      //if (compilationManager.isRebuild()) {
      //  FlexCompilationUtils.deleteCacheForFile(outputFilePath);
      //}

      //FlexCompilationUtils.ensureOutputFileWritable(myModule.getProject(), outputFilePath);
      doStart(compilationManager);
    }
    catch (IOException e) {
      compilationManager.addMessage(this, CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
      myCompilationFailed = true;
      cancel();
    }
  }

  private static List<File> createConfigFiles(final JpsFlexBuildConfiguration bc) throws IOException {
    final ArrayList<File> configFiles = new ArrayList<File>(2);
    //configFiles.add(CompilerConfigGenerator.getOrCreateConfigFile(bc));

    final String additionalConfigFilePath = bc.getCompilerOptions().getAdditionalConfigFilePath();
    if (!bc.isTempBCForCompilation() && !additionalConfigFilePath.isEmpty()) {
      final VirtualFile additionalConfigFile = LocalFileSystem.getInstance().findFileByPath(additionalConfigFilePath);
      if (additionalConfigFile == null) {
        throw new IOException(
          FlexBundle.message("additional.config.file.not.found.for.bc.0.of.module.1", additionalConfigFilePath, bc.getName(),
                             myModule.getName()));
      }
      configFiles.add(additionalConfigFile);
    }

    return configFiles;
  }*/
}
