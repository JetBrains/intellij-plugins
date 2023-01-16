package com.intellij.jps.flex.build;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.build.FlexResourceBuildTarget;
import com.intellij.flex.build.FlexResourceBuildTargetType;
import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsFlexCompilerOptions;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.PathUtilRt;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.builders.BuildOutputConsumer;
import org.jetbrains.jps.builders.BuildRootDescriptor;
import org.jetbrains.jps.builders.DirtyFilesHolder;
import org.jetbrains.jps.builders.FileProcessor;
import org.jetbrains.jps.builders.impl.OutputTracker;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.FSOperations;
import org.jetbrains.jps.incremental.ProjectBuildException;
import org.jetbrains.jps.incremental.TargetBuilder;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.incremental.messages.ProgressMessage;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.java.compiler.JpsCompilerExcludes;
import org.jetbrains.jps.model.java.compiler.JpsJavaCompilerConfiguration;
import org.jetbrains.jps.util.JpsPathUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class FlexResourceBuilder extends TargetBuilder<BuildRootDescriptor, FlexResourceBuildTarget> {

  private static final @NonNls String BUILDER_NAME = "Flash Resource Builder";

  protected FlexResourceBuilder() {
    super(Arrays.asList(FlexResourceBuildTargetType.PRODUCTION, FlexResourceBuildTargetType.TEST));
  }

  @Override
  @NotNull
  public String getPresentableName() {
    return BUILDER_NAME;
  }

  @Override
  public ExitCode buildTarget(@NotNull final FlexResourceBuildTarget target,
                              @NotNull final DirtyFilesHolder<BuildRootDescriptor, FlexResourceBuildTarget> holder,
                              @NotNull final BuildOutputConsumer outputConsumer,
                              @NotNull final CompileContext context) throws ProjectBuildException, IOException {

    final JpsJavaCompilerConfiguration configuration =
      JpsJavaExtensionService.getInstance().getCompilerConfiguration(target.getModule().getProject());
    final JpsCompilerExcludes excludes = configuration.getCompilerExcludes();

    final OutputTracker out = OutputTracker.create(outputConsumer);

    try {
      holder.processDirtyFiles(new FileProcessor<>() {
        @Override
        public boolean apply(final FlexResourceBuildTarget target, final File file, final BuildRootDescriptor root) throws IOException {
          if (excludes.isExcluded(file)) {
            return true;
          }

          final String relativePath = FileUtil.toSystemIndependentName(FileUtil.getRelativePath(root.getRootFile(), file));

          if (target.isTests()) {
            if (!FlexCommonUtils.isSourceFile(file.getName())) {
              final String outputRootUrl = JpsJavaExtensionService.getInstance().getOutputUrl(target.getModule(), target.isTests());
              if (outputRootUrl == null) {
                return true;
              }

              final String targetPath = JpsPathUtil.urlToPath(outputRootUrl) + '/' + relativePath;

              context.processMessage(new ProgressMessage("Copying " + file.getPath()));
              copyResource(context, file, Collections.singleton(targetPath), out);
            }
          }
          else {
            final Collection<String> targetPaths = new ArrayList<>();

            for (JpsFlexBuildConfiguration bc : target.getModule().getProperties().getBuildConfigurations()) {
              if (bc.isSkipCompile() || !FlexCommonUtils.canHaveResourceFiles(bc.getNature()) ||
                bc.getCompilerOptions().getResourceFilesMode() == JpsFlexCompilerOptions.ResourceFilesMode.None) {
                continue;
              }

              final JpsFlexCompilerOptions.ResourceFilesMode mode = bc.getCompilerOptions().getResourceFilesMode();
              if (mode == JpsFlexCompilerOptions.ResourceFilesMode.All && !FlexCommonUtils.isSourceFile(file.getName()) ||
                mode == JpsFlexCompilerOptions.ResourceFilesMode.ResourcePatterns && configuration.isResourceFile(file, root.getRootFile())) {
                final String outputFolder = PathUtilRt.getParentPath(bc.getActualOutputFilePath());
                targetPaths.add(outputFolder + "/" + relativePath);
              }
            }

            if (!targetPaths.isEmpty()) {
              context.processMessage(new ProgressMessage("Copying " + file.getPath()));
              copyResource(context, file, targetPaths, out);
            }
          }

          return true;
        }
      });
    }
    catch (Exception e) {
      throw new ProjectBuildException(e.getMessage(), e);
    }
    return out.isOutputGenerated()? ExitCode.OK : ExitCode.NOTHING_DONE;
  }

  private static void copyResource(final CompileContext context,
                                   final File file,
                                   final Collection<String> targetPaths,
                                   final BuildOutputConsumer outputConsumer) {
    try {
      for (String targetPath : targetPaths) {
        final File targetFile = new File(targetPath);
        FSOperations.copy(file, targetFile);
        outputConsumer.registerOutputFile(targetFile, Collections.singletonList(file.getPath()));
      }
    }
    catch (IOException e) {
      context.processMessage(new CompilerMessage(BUILDER_NAME, BuildMessage.Kind.ERROR, e.getMessage(),
                                                 FileUtil.toSystemIndependentName(file.getPath())));
    }
  }
}
