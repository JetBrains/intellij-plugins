package com.intellij.javascript.karma.util;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.RunProfileStarter;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.javascript.nodejs.CompletionModuleInfo;
import com.intellij.javascript.nodejs.NodeModuleSearchUtil;
import com.intellij.javascript.nodejs.NodePathSettings;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.ui.content.Content;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.io.LocalFileFinder;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class KarmaUtil {

  public static final String NODE_PACKAGE_NAME = "karma";
  private static final String[] BEFORE_EXT_PARTS = new String[] {".conf", "-conf"};
  private static final String[] EXTENSIONS = {"js", "coffee", "es6", "ts"};

  private KarmaUtil() {
  }

  public static void selectAndFocusIfNotDisposed(@NotNull RunnerLayoutUi ui,
                                                 @NotNull Content content,
                                                 boolean requestFocus,
                                                 boolean forced) {
    if (!ui.isDisposed()) {
      ui.selectAndFocus(content, requestFocus, forced);
    }
  }

  @NotNull
  public static List<VirtualFile> listPossibleConfigFilesInProject(@NotNull Project project) {
    GlobalSearchScope contentScope = ProjectScope.getContentScope(project);
    GlobalSearchScope scope = contentScope.intersectWith(GlobalSearchScope.notScope(ProjectScope.getLibrariesScope(project)));
    List<VirtualFile> result = ContainerUtil.newArrayList();
    List<FileType> fileTypes = JavaScriptFileType.getFileTypesCompilableToJavaScript();
    for (FileType type : fileTypes) {
      Collection<VirtualFile> files = FileTypeIndex.getFiles(type, scope);
      for (VirtualFile file : files) {
        if (file != null && file.isValid() && !file.isDirectory() && isKarmaConfigFile(file.getNameSequence())) {
          if (!JSLibraryUtil.isProbableLibraryFile(file)) {
            result.add(file);
          }
        }
      }
    }
    return result;
  }

  public static boolean isKarmaConfigFile(@NotNull CharSequence filename) {
    int len = filename.length();
    int extensionInd = StringUtil.lastIndexOf(filename, '.', 0, len);
    if (extensionInd == -1) {
      return false;
    }
    boolean extMatched = false;
    for (String ext : EXTENSIONS) {
      if (ext.length() == len - extensionInd - 1 && StringUtil.endsWith(filename, ext)) {
        extMatched = true;
        break;
      }
    }
    if (extMatched) {
      for (String beforeExt : BEFORE_EXT_PARTS) {
        if (CharArrayUtil.regionMatches(filename, extensionInd - beforeExt.length(), beforeExt)) {
          return true;
        }
      }
    }
    return false;
  }

  @Nullable
  public static VirtualFile getRequester(@NotNull Project project, @NotNull String configFilePath) {
    VirtualFile requester = null;
    if (StringUtil.isNotEmpty(configFilePath)) {
      File configFile = new File(configFilePath);
      if (configFile.isFile()) {
        requester = VfsUtil.findFileByIoFile(configFile, false);
      }
    }
    if (requester == null || !requester.isValid()) {
      requester = project.getBaseDir();
    }
    return requester;
  }

  public static boolean isPathUnderContentRoots(@NotNull Project project, @NotNull String filePath) {
    VirtualFile file = LocalFileFinder.findFile(FileUtil.toSystemIndependentName(filePath));
    if (file == null || !file.isValid()) {
      return false;
    }
    VirtualFile contentRoot = ProjectFileIndex.SERVICE.getInstance(project).getContentRootForFile(file, false);
    return contentRoot != null;
  }

  @Nullable
  public static String detectKarmaPackageDir(@NotNull Project project,
                                             @NotNull String configFilePath,
                                             @NotNull String nodeInterpreterPath) {
    List<CompletionModuleInfo> modules = ContainerUtil.newArrayList();
    VirtualFile requester = getRequester(project, configFilePath);
    NodePathSettings nodeSettings = StringUtil.isEmptyOrSpaces(nodeInterpreterPath) ? null : new NodePathSettings(nodeInterpreterPath);
    NodeModuleSearchUtil.findModulesWithName(modules,
                                             NODE_PACKAGE_NAME,
                                             requester,
                                             nodeSettings,
                                             true);
    for (CompletionModuleInfo module : modules) {
      VirtualFile moduleRoot = module.getVirtualFile();
      if (moduleRoot != null && moduleRoot.isValid() && moduleRoot.isDirectory()) {
        return FileUtil.toSystemDependentName(moduleRoot.getPath());
      }
    }
    return null;
  }

  @NotNull
  public static RunContentDescriptor createDefaultDescriptor(@NotNull ExecutionResult executionResult,
                                                             @NotNull ExecutionEnvironment environment) {
    RunContentBuilder contentBuilder = new RunContentBuilder(executionResult, environment);
    return contentBuilder.showRunContent(environment.getContentToReuse());
  }

  @NotNull
  public static RunProfileStarter createDefaultRunProfileStarter(@NotNull final ExecutionResult executionResult) {
    return new RunProfileStarter() {
      @Nullable
      @Override
      public RunContentDescriptor execute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment environment)
        throws ExecutionException {
        return createDefaultDescriptor(executionResult, environment);
      }
    };
  }
}
