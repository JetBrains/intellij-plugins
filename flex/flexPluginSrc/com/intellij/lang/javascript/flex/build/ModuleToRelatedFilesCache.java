package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessage;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This cache is used to skip module compilation if nothing has changed since last successful compilation.
 * Module is either Flex module or Java module with Flex facets.
 */
public class ModuleToRelatedFilesCache {

  private static final Pattern OUTPUT_FILE_IS_UP_TO_DATE =
    Pattern.compile("(\\[.*\\] )?(.+) is up-to-date and does not have to be rebuilt.");

  private final Project myProject;
  /**
   * Presence of module in this map means that nothing was changed since last compilation.
   * If the file that module depends on is a directory then it is handled recursively.
   * For example adding sdk root to this cache is enough to handle compilation dependency on all sdk files.
   */
  public final Map<Module, Set<VirtualFile>> myModuleToRelatedFiles = new THashMap<Module, Set<VirtualFile>>();

  private static final String OUTPUT_FILE_TAG = "<flex-config><output>";

  private static final String[] TAGS_FOR_FILE_PATHS_IN_CONFIG_FILE =
    {"<flex-config><compiler><external-library-path><path-element>", "<flex-config><compiler><local-font-paths><path-element>",
      "<flex-config><compiler><library-path><path-element>", "<flex-config><compiler><namespaces><namespace><manifest>",
      "<flex-config><compiler><source-path><path-element>", "<flex-config><compiler><theme><filename>", "<flex-config><include-file><path>",
      "<flex-config><include-sources><path-element>", "<flex-config><include-stylesheet><path>", "<flex-config><file-specs><path-element>",
      "<flex-config><compiler><include-libraries><library>"
      // "<flex-config><output>"   intentionally excluded, because already handled
    };


  public ModuleToRelatedFilesCache(final Project project) {
    myProject = project;
  }

  public void clear() {
    myModuleToRelatedFiles.clear();
  }

  public boolean isNothingChangedSincePreviousCompilation(final Module module) {
    return myModuleToRelatedFiles.containsKey(module);
  }

  public void markModuleAndDependentModulesDirty(final Module module) {
    if (myModuleToRelatedFiles.remove(module) != null) {
      clearForDependentModules(module);
    }
  }

  public void markDependentModulesDirty(final VirtualFile file) {
    if (myModuleToRelatedFiles.isEmpty()) return;

    clearModulesIfSourceRoot(file);
    clearModulesThatDependOnFile(file);
  }

  public void cacheModuleWithDependencies(final CompileContext context,
                                          final Module module,
                                          final Collection<List<VirtualFile>> allConfigFiles) {
    myModuleToRelatedFiles.remove(module);
    for (List<VirtualFile> configFiles : allConfigFiles) {
      cacheModuleWithDependencies(context, module, configFiles, true);
    }
  }

  public void cacheModuleWithDependencies(final CompileContext context, final Module module, final List<VirtualFile> configFiles) {
    myModuleToRelatedFiles.remove(module);
    cacheModuleWithDependencies(context, module, configFiles, false);
  }

  private void cacheModuleWithDependencies(final CompileContext context,
                                           final Module module,
                                           final List<VirtualFile> configFiles,
                                           final boolean append) {
    final VirtualFile outputFile = getOutputFile(context.getMessages(CompilerMessageCategory.INFORMATION), configFiles);
    if (outputFile != null) {
      final HashSet<VirtualFile> dependencies = new HashSet<VirtualFile>();
      dependencies.add(outputFile);
      final Sdk sdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(module);
      if (sdk != null) {
        dependencies.add(sdk.getHomeDirectory());
      }

      for (VirtualFile configFile : configFiles) {
        dependencies.add(configFile);
        dependencies.addAll(getDependenciesExceptSourceRootsFromConfigFile(module, configFile));
      }

      appendCssAndRespectiveSwfFiles(dependencies, module);

      if (append) {
        final Set<VirtualFile> files = myModuleToRelatedFiles.get(module);
        if (files == null) {
          myModuleToRelatedFiles.put(module, dependencies);
        }
        else {
          files.addAll(dependencies);
        }
      }
      else {
        myModuleToRelatedFiles.put(module, dependencies);
      }
    }
  }

  private static void appendCssAndRespectiveSwfFiles(final Set<VirtualFile> dependencies, final Module module) {
    final LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
    final ModuleFileIndex fileIndex = ModuleRootManager.getInstance(module).getFileIndex();

    for (final FlexBuildConfiguration config : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module)) {
      if (config.DO_BUILD) {
        for (final String cssFilePath : config.CSS_FILES_LIST) {
          final VirtualFile cssFile = localFileSystem.findFileByPath(cssFilePath);
          if (cssFile != null && !fileIndex.isInSourceContent(cssFile)) {
            dependencies.add(cssFile);
          }

          final String swfFilePath = FlexCompilationUtils.getOutputSwfFilePathForCssFile(cssFilePath, config);
          VirtualFile swfFile = localFileSystem.findFileByPath(swfFilePath);
          if (swfFile == null) {
            swfFile = FlexCompilationManager.refreshAndFindFileInWriteAction(module.getProject(), swfFilePath);
          }

          if (swfFile != null) {
            dependencies.add(swfFile);
          }
        }
      }
    }
  }

  private void clearForDependentModules(final Module module) {
    if (!myModuleToRelatedFiles.isEmpty()) {

      final Runnable runnable = new Runnable() {
        public void run() {
          for (Module dependentModule : ModuleUtil.getAllDependentModules(module)) {
            if (myModuleToRelatedFiles.remove(dependentModule) != null) {
              clearForDependentModules(dependentModule);
            }
          }
        }
      };


      if (ApplicationManager.getApplication().isReadAccessAllowed()) {
        runnable.run();
      }
      else {
        ApplicationManager.getApplication().runReadAction(runnable);
      }
    }
  }

  @Nullable
  private VirtualFile getOutputFile(final CompilerMessage[] messages, final List<VirtualFile> configFiles) {
    try {
      final LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
      final VirtualFile configFile = configFiles.get(configFiles.size() - 1);
      final String outputFilePath = FlexUtils.findXMLElement(configFile.getInputStream(), OUTPUT_FILE_TAG);
      if (outputFilePath == null) return null;
      final VirtualFile outputFile =
        FlexCompilationManager.refreshAndFindFileInWriteAction(myProject, outputFilePath, configFile.getParent().getPath());
      if (outputFile == null) return null;

      for (final CompilerMessage message : messages) {
        final String text = message.getMessage();
        final Matcher matcher1 = FlexCompilationManager.OUTPUT_FILE_CREATED_PATTERN.matcher(text);
        if (matcher1.matches()) {
          final String outputFilePath1 = matcher1.group(2);
          final int size;
          size = Integer.parseInt(matcher1.group(3));
          if (outputFile.equals(localFileSystem.findFileByPath(outputFilePath1)) && size == outputFile.getLength()) {
            return outputFile;
          }
        }
        else {
          final Matcher matcher2 = OUTPUT_FILE_IS_UP_TO_DATE.matcher(text);
          if (matcher2.matches()) {
            final String outputFilePath2 = matcher2.group(2);
            if (outputFile.equals(localFileSystem.findFileByPath(outputFilePath2))) {
              return outputFile;
            }
          }
        }
      }
    }
    catch (NumberFormatException e) {/*ignore*/}
    catch (IOException e) {/*ignore*/}
    return null;
  }

  private static Collection<VirtualFile> getDependenciesExceptSourceRootsFromConfigFile(final Module module, final VirtualFile configFile) {
    final List<VirtualFile> result = new ArrayList<VirtualFile>();
    final LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
    final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();

    try {
      final Map<String, List<String>> elementsMap =
        FlexUtils.findXMLElements(configFile.getInputStream(), Arrays.asList(TAGS_FOR_FILE_PATHS_IN_CONFIG_FILE));
      for (List<String> filePathList : elementsMap.values()) {
        for (String filePath : filePathList) {
          if (filePath.endsWith(FlexCompilerHandler.LOCALE_TOKEN)) {
            filePath = filePath.substring(0, filePath.length() - FlexCompilerHandler.LOCALE_TOKEN.length());
          }

          VirtualFile file = localFileSystem.findFileByPath(filePath);
          if (file == null) {
            final VirtualFile dir = FlexUtils.getFlexCompilerWorkDir(module.getProject(), null);
            file = VfsUtil.findRelativeFile(filePath, dir);
            if (file == null && dir != configFile.getParent()) {
              file = VfsUtil.findRelativeFile(filePath, configFile.getParent());
            }
          }

          if (file != null) {
            boolean inSourceRoot = false;
            for (VirtualFile sourceRoot : sourceRoots) {
              if (VfsUtil.isAncestor(sourceRoot, file, false)) {
                inSourceRoot = true;
                break;
              }
            }

            if (!inSourceRoot) {
              result.add(file);
            }
          }
        }
      }
    }
    catch (IOException e) {/*ignore*/}
    return result;
  }

  private void clearModulesIfSourceRoot(final VirtualFile file) {
    final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
    final Module module = fileIndex.getModuleForFile(file);
    if (module != null) {
      final VirtualFile sourceRoot = fileIndex.getSourceRootForFile(file);
      if (sourceRoot != null) {
        markModuleAndDependentModulesDirty(module);
      }
    }
  }

  private void clearModulesThatDependOnFile(final VirtualFile file) {
    final Collection<Module> modulesToClear = new ArrayList<Module>();
    for (Map.Entry<Module, Set<VirtualFile>> entry : myModuleToRelatedFiles.entrySet()) {
      final Set<VirtualFile> files = entry.getValue();
      for (VirtualFile dependencyFile : files) {
        if (dependencyFile.equals(file) ||
            (dependencyFile.isDirectory() && VfsUtil.isAncestor(dependencyFile, file, false)) ||
            (file.isDirectory() && VfsUtil.isAncestor(file, dependencyFile, false))) {
          modulesToClear.add(entry.getKey());
          break;
        }
      }
    }

    for (Module module : modulesToClear) {
      markModuleAndDependentModulesDirty(module);
    }
  }
}
