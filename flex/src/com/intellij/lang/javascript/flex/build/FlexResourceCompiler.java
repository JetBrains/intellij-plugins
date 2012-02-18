package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.CompilerConfiguration;
import com.intellij.compiler.impl.CompilerUtil;
import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.flexunit.NewFlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.compiler.ex.CompileContextEx;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.Chunk;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.intellij.lang.javascript.flex.projectStructure.model.CompilerOptions.ResourceFilesMode;

public class FlexResourceCompiler implements TranslatingCompiler {
  private final Project myProject;
  private final CompilerConfiguration myConfiguration;

  public FlexResourceCompiler(final Project project, final CompilerConfiguration compilerConfiguration) {
    myProject = project;
    myConfiguration = compilerConfiguration;
  }

  @NotNull
  public String getDescription() {
    return "Flex Resource Compiler";
  }

  public boolean validateConfiguration(CompileScope scope) {
    return true;
  }

  public boolean isCompilableFile(final VirtualFile file, final CompileContext context) {
    final Module module = context.getModuleByFile(file);
    return module != null && ModuleType.get(module) == FlexModuleType.getInstance();
  }

  public void compile(final CompileContext context, Chunk<Module> moduleChunk, final VirtualFile[] files, final OutputSink sink) {
    context.getProgressIndicator().pushState();
    context.getProgressIndicator().setText(CompilerBundle.message("progress.copying.resources"));

    final Map<String, Collection<OutputItem>> processed = new HashMap<String, Collection<OutputItem>>();
    final LinkedList<CopyCommand> copyCommands = new LinkedList<CopyCommand>();
    final Module singleChunkModule = moduleChunk.getNodes().size() == 1 ? moduleChunk.getNodes().iterator().next() : null;

    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        // Copy test resources only if FlexUnit is going to be launched and only for corresponding module
        final RunConfiguration runConfig = CompileStepBeforeRun.getRunConfiguration(context);
        final String testModuleName = runConfig instanceof NewFlexUnitRunConfiguration
                                      ? ((NewFlexUnitRunConfiguration)runConfig).getRunnerParameters().getModuleName()
                                      : null;
        final Module testModule = testModuleName == null ? null : ModuleManager.getInstance(myProject).findModuleByName(testModuleName);

        final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();

        for (final VirtualFile file : files) {
          if (context.getProgressIndicator().isCanceled()) {
            break;
          }

          final Module module = singleChunkModule != null ? singleChunkModule : context.getModuleByFile(file);
          if (module == null) {
            continue; // looks like file invalidated
          }

          final VirtualFile sourceRoot = fileIndex.getSourceRootForFile(file);
          if (sourceRoot == null) {
            continue;
          }
          final String filePath = file.getPath();
          final String relativePath = VfsUtilCore.getRelativePath(file, sourceRoot, '/');
          final boolean inTests = ((CompileContextEx)context).isInTestSourceContent(file);

          final Collection<Pair<String, String>> outputDirPathsAndTargetFilePaths = new ArrayList<Pair<String, String>>();

          if (inTests) {
            if (module == testModule) {
              final VirtualFile outputDir = context.getModuleOutputDirectoryForTests(module);
              addOutputDirPathAndTargetFilePath(outputDirPathsAndTargetFilePaths, sourceRoot, outputDir, relativePath, fileIndex);
            }
          }
          else {
            for (FlexIdeBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
              if (!bc.isSkipCompile() && BCUtils.canHaveResourceFiles(bc.getNature())) {
                final ResourceFilesMode mode = bc.getCompilerOptions().getResourceFilesMode();
                if (mode == ResourceFilesMode.All && !isSourceFile(file) ||
                    mode == ResourceFilesMode.ResourcePatterns && myConfiguration.isResourceFile(file)) {
                  final VirtualFile outputDir = LocalFileSystem.getInstance().findFileByPath(bc.getOutputFolder());
                  addOutputDirPathAndTargetFilePath(outputDirPathsAndTargetFilePaths, sourceRoot, outputDir, relativePath, fileIndex);
                }
              }
            }
          }

          for (Pair<String, String> outputDirPathAndTargetFilePath : outputDirPathsAndTargetFilePaths) {
            final String outputDirPath = outputDirPathAndTargetFilePath.first;
            final String targetFilePath = outputDirPathAndTargetFilePath.second;
            if (filePath.equals(targetFilePath)) {
              addToMap(processed, outputDirPath, new MyOutputItem(targetFilePath, file));
            }
            else {
              copyCommands.add(new CopyCommand(outputDirPath, filePath, targetFilePath, file));
            }
          }
        }
      }
    });

    final List<File> filesToRefresh = new ArrayList<File>();
    // do actual copy outside of read action to reduce the time the application is locked on it
    while (!copyCommands.isEmpty()) {
      final CopyCommand command = copyCommands.removeFirst();
      if (context.getProgressIndicator().isCanceled()) {
        break;
      }
      //context.getProgressIndicator().setFraction((idx++) * 1.0 / total);
      context.getProgressIndicator().setText2("Copying " + command.getFromPath() + "...");
      try {
        final MyOutputItem outputItem = command.copy(filesToRefresh);
        addToMap(processed, command.getOutputDirPath(), outputItem);
      }
      catch (IOException e) {
        context.addMessage(
          CompilerMessageCategory.ERROR,
          CompilerBundle.message("error.copying", command.getFromPath(), command.getToPath(), e.getMessage()),
          command.getSourceFileUrl(), -1, -1
        );
      }
    }

    if (!filesToRefresh.isEmpty()) {
      CompilerUtil.refreshIOFiles(filesToRefresh);
      filesToRefresh.clear();
    }

    for (Iterator<Map.Entry<String, Collection<OutputItem>>> it = processed.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<String, Collection<OutputItem>> entry = it.next();
      sink.add(entry.getKey(), entry.getValue(), VirtualFile.EMPTY_ARRAY);
      it.remove(); // to free memory
    }
    context.getProgressIndicator().popState();
  }

  private static boolean isSourceFile(final VirtualFile file) {
    final String ext = file.getExtension();
    return ext != null && (ext.equalsIgnoreCase("as") || ext.equalsIgnoreCase("mxml") || ext.equalsIgnoreCase("fxg"));
  }

  private static void addOutputDirPathAndTargetFilePath(final Collection<Pair<String, String>> outputDirPathsAndTargetFilePaths,
                                                        final VirtualFile sourceRoot,
                                                        final VirtualFile outputDir,
                                                        final String relativePath,
                                                        final ProjectFileIndex fileIndex) {
    if (outputDir == null) {
      return;
    }
    final String outputDirPath = outputDir.getPath();

    final String packagePrefix = fileIndex.getPackageNameByDirectory(sourceRoot);
    final String targetFilePath;
    if (packagePrefix != null && packagePrefix.length() > 0) {
      targetFilePath = outputDirPath + "/" + packagePrefix.replace('.', '/') + "/" + relativePath;
    }
    else {
      targetFilePath = outputDirPath + "/" + relativePath;
    }
    outputDirPathsAndTargetFilePaths.add(Pair.create(outputDirPath, targetFilePath));
  }

  private static void addToMap(Map<String, Collection<OutputItem>> map, String outputDir, OutputItem item) {
    Collection<OutputItem> list = map.get(outputDir);
    if (list == null) {
      list = new ArrayList<OutputItem>();
      map.put(outputDir, list);
    }
    list.add(item);
  }

  private static class CopyCommand {
    private final String myOutputDirPath;
    private final String myFromPath;
    private final String myToPath;
    private final VirtualFile mySourceFile;

    private CopyCommand(String outputDirPath, String fromPath, String toPath, VirtualFile sourceFile) {
      myOutputDirPath = outputDirPath;
      myFromPath = fromPath;
      myToPath = toPath;
      mySourceFile = sourceFile;
    }

    public MyOutputItem copy(List<File> filesToRefresh) throws IOException {
      final File targetFile = new File(myToPath);
      FileUtil.copyContent(new File(myFromPath), targetFile);
      filesToRefresh.add(targetFile);
      return new MyOutputItem(myToPath, mySourceFile);
    }

    public String getOutputDirPath() {
      return myOutputDirPath;
    }

    public String getFromPath() {
      return myFromPath;
    }

    public String getToPath() {
      return myToPath;
    }

    public String getSourceFileUrl() {
      // do not use mySourceFile.getUrl() directly as it requires read action
      return VirtualFileManager.constructUrl(mySourceFile.getFileSystem().getProtocol(), myFromPath);
    }
  }

  private static class MyOutputItem implements OutputItem {
    private final String myTargetPath;
    private final VirtualFile myFile;

    private MyOutputItem(String targetPath, VirtualFile sourceFile) {
      myTargetPath = targetPath;
      myFile = sourceFile;
    }

    public String getOutputPath() {
      return myTargetPath;
    }

    public VirtualFile getSourceFile() {
      return myFile;
    }
  }
}
