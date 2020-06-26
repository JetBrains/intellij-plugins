// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.util;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentDescriptorReusePolicy;
import com.intellij.javascript.karma.execution.KarmaConsoleView;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageDescriptor;
import com.intellij.lang.javascript.JSLanguageUtil;
import com.intellij.lang.javascript.library.JSLibraryUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.util.ObjectUtils;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.io.LocalFileFinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class KarmaUtil {

  public static final String KARMA_PACKAGE_NAME = "karma";
  public static final String ANGULAR_CLI__PACKAGE_NAME = "@angular/cli";
  public static final NodePackageDescriptor PKG_DESCRIPTOR = new NodePackageDescriptor(ANGULAR_CLI__PACKAGE_NAME, KARMA_PACKAGE_NAME);
  private static final String[] STARTING_PARTS = new String[] {"karma"};
  private static final String NAME_PART_DELIMITERS = ".-";
  private static final String[] BEFORE_EXT_PARTS = new String[] {"conf", "karma"};
  private static final String[] EXTENSIONS = {"js", "coffee", "es6", "ts"};
  private static final String[] MOST_RELEVANT_NAMES = {"karma.conf", "karma-conf", "karma-js.conf"};

  private KarmaUtil() {
  }

  @NotNull
  public static List<VirtualFile> listPossibleConfigFilesInProject(@NotNull Project project) {
    GlobalSearchScope contentScope = ProjectScope.getContentScope(project);
    GlobalSearchScope scope = contentScope.intersectWith(GlobalSearchScope.notScope(ProjectScope.getLibrariesScope(project)));
    List<VirtualFile> result = new ArrayList<>();
    for (FileType type : JSLanguageUtil.getFileTypesCompilableToJavaScript()) {
      Collection<VirtualFile> files = FileTypeIndex.getFiles(type, scope);
      for (VirtualFile file : files) {
        if (file != null && file.isValid() && !file.isDirectory() && isKarmaConfigFile(file.getNameSequence(), false)) {
          if (!JSLibraryUtil.isProbableLibraryFile(file)) {
            result.add(file);
          }
        }
      }
    }
    return result;
  }

  public static boolean isKarmaConfigFile(@NotNull CharSequence filename, boolean matchMostRelevantNamesOnly) {
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
    if (matchMostRelevantNamesOnly) {
      return isStartingPartMatched(filename, MOST_RELEVANT_NAMES);
    }
    if (!extMatched) {
      return false;
    }
    for (String beforeExtPart : BEFORE_EXT_PARTS) {
      int startOffset = extensionInd - beforeExtPart.length();
      if (startOffset > 0 && CharArrayUtil.regionMatches(filename, startOffset, beforeExtPart)) {
        if (NAME_PART_DELIMITERS.indexOf(filename.charAt(startOffset - 1)) >= 0) {
          return !StringUtil.contains(filename, "protractor");
        }
      }
    }
    return isStartingPartMatched(filename, STARTING_PARTS);
  }

  private static boolean isStartingPartMatched(@NotNull CharSequence filename, String @NotNull [] startingParts) {
    for (String startingPart : startingParts) {
      if (startingPart.length() < filename.length() && CharArrayUtil.regionMatches(filename, 0, startingPart)) {
        if (NAME_PART_DELIMITERS.indexOf(filename.charAt(startingPart.length())) >= 0) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isPathUnderContentRoots(@NotNull Project project, @NotNull NodePackage pkg) {
    VirtualFile file = LocalFileFinder.findFile(pkg.getSystemIndependentPath());
    if (file == null || !file.isValid()) {
      return false;
    }
    VirtualFile contentRoot = JSLibraryUtil.getContentRootForFile(project, file);
    return contentRoot != null;
  }

  @NotNull
  public static RunContentDescriptor createDefaultDescriptor(@NotNull ExecutionResult executionResult,
                                                             @NotNull ExecutionEnvironment environment) {
    ExecutionConsole console = executionResult.getExecutionConsole();
    KarmaServer server = console instanceof KarmaConsoleView ? ((KarmaConsoleView)console).getKarmaServer() : null;
    RunContentBuilder contentBuilder = new RunContentBuilder(executionResult, environment);
    RunContentDescriptor descriptor = contentBuilder.showRunContent(environment.getContentToReuse());
    return withReusePolicy(descriptor, server);
  }

  @NotNull
  public static RunContentDescriptor withReusePolicy(@NotNull RunContentDescriptor descriptor, @Nullable KarmaServer karmaServer) {
    descriptor.setReusePolicy(new RunContentDescriptorReusePolicy() {
      @Override
      public boolean canBeReusedBy(@NotNull RunContentDescriptor newDescriptor) {
        if (karmaServer == null || karmaServer.getProcessHandler().isProcessTerminated()) {
          return true;
        }
        KarmaConsoleView newConsole = ObjectUtils.tryCast(newDescriptor.getExecutionConsole(), KarmaConsoleView.class);
        return newConsole != null && newConsole.getKarmaServer().equals(karmaServer);
      }
    });
    return descriptor;
  }

  public static boolean isAngularCliPkg(@NotNull NodePackage pkg) {
    return pkg.getSystemIndependentPath().endsWith("/" + ANGULAR_CLI__PACKAGE_NAME);
  }
}
