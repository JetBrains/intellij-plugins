package com.intellij.jps.flex.build;

import com.intellij.flex.build.FlexBuildTarget;
import com.intellij.flex.build.FlexBuildTargetType;
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
  protected FlexBuilder() {
    super(Collections.singletonList(FlexBuildTargetType.INSTANCE));
  }

  @NotNull
  public String getPresentableName() {
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
    configFiles.add(CompilerConfigGeneratorRt.getOrCreateConfigFile(bc));

    final String additionalConfigFilePath = bc.getCompilerOptions().getAdditionalConfigFilePath();
    if (!bc.isTempBCForCompilation() && !additionalConfigFilePath.isEmpty()) {
      final File additionalConfigFile = new File(additionalConfigFilePath);
      if (!additionalConfigFile.isFile()) {
        throw new IOException(
          FlexCommonBundle.message("additional.config.file.not.found.for.bc.0.of.module.1", additionalConfigFilePath, bc.getName(),
                                   bc.getModule().getName()));
      }
      configFiles.add(additionalConfigFile);
    }

    return configFiles;
  }*/
}
