package com.intellij.jps.flex.build;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsFlexBuildConfigurationManager;
import com.intellij.flex.model.bc.JpsFlexCompilerOptions;
import com.intellij.flex.model.module.JpsFlexModuleType;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.jps.JpsPathUtil;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.incremental.*;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.jetbrains.jps.incremental.messages.ProgressMessage;
import org.jetbrains.jps.incremental.storage.SourceToOutputMapping;
import org.jetbrains.jps.model.java.JpsJavaExtensionService;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.module.JpsTypedModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class FlexResourceBuilder extends ModuleLevelBuilder {

  private static final @NonNls String BUILDER_NAME = "flex-resource";

  protected FlexResourceBuilder() {
    super(BuilderCategory.SOURCE_PROCESSOR);
  }

  public String getName() {
    return BUILDER_NAME;
  }

  public String getDescription() {
    return "Flex Resource Builder";
  }

  public ExitCode build(final CompileContext context, final ModuleChunk chunk) throws ProjectBuildException {
    final ResourcePatterns patterns = ResourcePatterns.KEY.get(context);
    assert patterns != null;

    final Ref<Boolean> doneSomething = new Ref<Boolean>(false);

    try {
      FSOperations.processFilesToRecompile(context, chunk, JpsFlexModuleType.INSTANCE, new FileProcessor() {
        public boolean apply(final JpsModule module, final File file, final String sourceRoot) throws IOException {
          final JpsTypedModule<JpsFlexBuildConfigurationManager> flexModule = module.asTyped(JpsFlexModuleType.INSTANCE);
          assert flexModule != null; // filtered by moduleTypeFilter

          final String relativePath = FileUtil.getRelativePath(sourceRoot, FileUtil.toSystemIndependentName(file.getPath()), '/');

          final SourceToOutputMapping sourceToOutputMap =
            context.getProjectDescriptor().dataManager.getSourceToOutputMap(module.getName(), context.isCompilingTests());

          if (context.isCompilingTests()) {
            if (!FlexCommonUtils.isSourceFile(file.getName())) {
              final String outputRootUrl = JpsJavaExtensionService.getInstance().getOutputUrl(module, context.isCompilingTests());
              if (outputRootUrl == null) return true;

              final String targetPath = JpsPathUtil.urlToPath(outputRootUrl) + '/' + relativePath;

              context.processMessage(new ProgressMessage("Copying " + file.getPath()));
              doneSomething.set(true);
              copyResource(context, file, Collections.singleton(targetPath), sourceToOutputMap);
            }
          }
          else {
            final Collection<String> targetPaths = new ArrayList<String>();

            for (JpsFlexBuildConfiguration bc : flexModule.getProperties().getBuildConfigurations()) {
              if (bc.isSkipCompile() || !FlexCommonUtils.canHaveResourceFiles(bc.getNature()) ||
                  bc.getCompilerOptions().getResourceFilesMode() == JpsFlexCompilerOptions.ResourceFilesMode.None) {
                continue;
              }

              final JpsFlexCompilerOptions.ResourceFilesMode mode = bc.getCompilerOptions().getResourceFilesMode();
              if (mode == JpsFlexCompilerOptions.ResourceFilesMode.All && !FlexCommonUtils.isSourceFile(file.getName()) ||
                  mode == JpsFlexCompilerOptions.ResourceFilesMode.ResourcePatterns && patterns.isResourceFile(file, sourceRoot)) {
                final String outputFolder = JpsPathUtil.getParentPath(bc.getActualOutputFilePath());
                targetPaths.add(outputFolder + "/" + relativePath);
              }
            }

            if (!targetPaths.isEmpty()) {
              context.processMessage(new ProgressMessage("Copying " + file.getPath()));
              doneSomething.set(true);
              copyResource(context, file, targetPaths, sourceToOutputMap);
            }
          }

          return true;
        }
      });

      return doneSomething.get() ? ExitCode.OK : ExitCode.NOTHING_DONE;
    }
    catch (Exception e) {
      throw new ProjectBuildException(e.getMessage(), e);
    }
  }

  private static void copyResource(final CompileContext context,
                                   final File file,
                                   final Collection<String> targetPaths,
                                   final SourceToOutputMapping sourceToOutputMapping) {
    try {
      for (String targetPath : targetPaths) {
        FileUtil.copyContent(file, new File(targetPath));
        context.processMessage(new CompilerMessage("Copied", BuildMessage.Kind.INFO, file.getPath() + " to " + targetPath));
      }

      try {
        sourceToOutputMapping.update(file.getPath(), targetPaths);
      }
      catch (Exception e) {
        context.processMessage(new CompilerMessage(BUILDER_NAME, e));
      }
    }
    catch (IOException e) {
      context.processMessage(new CompilerMessage("Flex Resource Compiler", BuildMessage.Kind.ERROR, e.getMessage(),
                                                 FileUtil.toSystemIndependentName(file.getPath())));
    }
  }
}
