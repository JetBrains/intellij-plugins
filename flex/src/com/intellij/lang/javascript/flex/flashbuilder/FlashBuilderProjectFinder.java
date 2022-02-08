// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.CommonBundle;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileFilters;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class FlashBuilderProjectFinder {

  public static final String PROJECT_PREFS_RELATIVE_PATH =
    "/.metadata/.plugins/org.eclipse.core.runtime/.settings/com.adobe.flexbuilder.project.prefs";
  static final String SDKS_RELATIVE_PATH = "/sdks";

  private static final String PROJECTS_CACHE_RELATIVE_PATH = "/.metadata/.plugins/org.eclipse.core.resources/.projects";
  private static final String DOT_LOCATION = ".location";

  static boolean isFlashBuilderWorkspace(final VirtualFile file) {
    return file != null && file.isDirectory() && VfsUtilCore.findRelativeFile(PROJECT_PREFS_RELATIVE_PATH, file) != null;
  }

  static boolean isFlashBuilderWorkspace(final String dirPath) {
    return new File(dirPath + PROJECT_PREFS_RELATIVE_PATH).isFile();
  }

  static boolean isFlashBuilderProject(final VirtualFile file) {
    final VirtualFile dir = file == null ? null : file.getParent();
    return file != null &&
           !file.isDirectory() &&
           FlashBuilderImporter.DOT_PROJECT.equals(file.getName()) &&
           dir != null &&
           dir.findChild(FlashBuilderImporter.DOT_ACTION_SCRIPT_PROPERTIES) != null;
  }

  static boolean isFlashBuilderProject(final File file) {
    final File dir = file == null ? null : file.getParentFile();
    final File dotActionScriptPropertiesFile = dir == null ? null : new File(dir, FlashBuilderImporter.DOT_ACTION_SCRIPT_PROPERTIES);
    return file != null &&
           FlashBuilderImporter.DOT_PROJECT.equals(file.getName()) &&
           file.isFile() &&
           dotActionScriptPropertiesFile != null &&
           dotActionScriptPropertiesFile.isFile();
  }

  static boolean collectAllProjectPaths(final @Nullable Project project, final List<String> projectPaths, final String dirPath) {
    final Runnable runnable = () -> {
      if (isFlashBuilderWorkspace(dirPath)) {
        collectProjectPathsInWorkspace(projectPaths, dirPath);
      }
      else {
        collectProjectPathsInDirectory(projectPaths, dirPath);
      }
    };

    return ProgressManager.getInstance()
      .runProcessWithProgressSynchronously(runnable, FlexBundle.message("looking.for.flash.builder.projects"), true, project);
  }

  private static void collectProjectPathsInWorkspace(final List<String> projectPaths, final String workspacePath) {
    final File projectsCacheDir = new File(workspacePath, PROJECTS_CACHE_RELATIVE_PATH);
    if (!projectsCacheDir.isDirectory()) return;

    final File[] subdirs = projectsCacheDir.listFiles(FileFilters.DIRECTORIES);
    for (File dir : subdirs) {
      final String dotProjectFileLocation = getDotProjectFileLocation(workspacePath, dir);
      if (dotProjectFileLocation != null) {
        projectPaths.add(dotProjectFileLocation);
      }
    }
  }

  /*
    the logic is taken from
    org.eclipse.core.internal.resources.LocalMetaArea.readPrivateDescription(IProject target, IProjectDescription description)
   */
  @Nullable
  private static String getDotProjectFileLocation(final String workspacePath, final File projectCacheDir) {
    final String projectName = projectCacheDir.getName();

    final File dotLocationFile = new File(projectCacheDir + "/" + DOT_LOCATION);
    if (dotLocationFile.isFile()) {
      try (DataInputStream input = new DataInputStream(new FileInputStream(dotLocationFile))) {
        final int CHUNK_START_LENGTH = 16;
        final String URI_PREFIX = "URI//";

        final long skipped = input.skip(CHUNK_START_LENGTH);
        if (skipped == CHUNK_START_LENGTH) {
          final String projectUriUri = input.readUTF();
          if (projectUriUri.startsWith(URI_PREFIX)) {
            final URI dotProjectUri = new URI(projectUriUri.substring(URI_PREFIX.length()) + "/" + FlashBuilderImporter.DOT_PROJECT);
            final File dotProjectFile = new File(dotProjectUri);
            if (isFlashBuilderProject(dotProjectFile)) {
              return dotProjectFile.getPath();
            }
          }
        }
      }
      catch (IOException | URISyntaxException e) {/*ignore*/}
    }

    // this code is reached if no information was found in '.location' file. It means default project location.
    final File dotProjectFile = new File(workspacePath + "/" + projectName + "/" + FlashBuilderImporter.DOT_PROJECT);
    if (isFlashBuilderProject(dotProjectFile)) {
      return dotProjectFile.getPath();
    }

    return null;
  }

  private static void collectProjectPathsInDirectory(final List<String> projectPaths, final String dirPath) {
    final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
    if (progressIndicator != null) {
      progressIndicator.checkCanceled();
      progressIndicator.setText2(dirPath);
    }

    final File dotProjectFile = new File(dirPath, FlashBuilderImporter.DOT_PROJECT);
    if (isFlashBuilderProject(dotProjectFile)) {
      projectPaths.add(dotProjectFile.getPath());
    }
    else {
      final File root = new File(dirPath);
      final File[] subdirs = root.listFiles(FileFilters.DIRECTORIES);
      for (final File subdir : subdirs) {
        collectProjectPathsInDirectory(projectPaths, subdir.getPath());
      }
    }
  }

  static boolean hasArchiveExtension(final String path) {
    return path.endsWith(FlashBuilderImporter.DOT_FXP) ||
           path.endsWith(FlashBuilderImporter.DOT_FXPL) ||
           path.endsWith(FlashBuilderImporter.DOT_ZIP);
  }

  static boolean hasFxpExtension(final String path) {
    final String lowercased = StringUtil.toLowerCase(path);
    return lowercased.endsWith(FlashBuilderImporter.DOT_FXP) || lowercased.endsWith(FlashBuilderImporter.DOT_FXPL);
  }

  static void checkArchiveContainsFBProject(final String archiveFilePath) throws ConfigurationException {
    _isMultiProjectArchive(archiveFilePath);
  }

  static boolean isMultiProjectArchive(final String archiveFilePath) {
    try {
      return _isMultiProjectArchive(archiveFilePath);
    }
    catch (ConfigurationException e) {
      return false;
    }
  }

  private static boolean _isMultiProjectArchive(final String archiveFilePath) throws ConfigurationException {
    boolean containsDotProjectFile = false;
    boolean containsDotActionScriptFile = false;
    boolean containsNestedProjects = false;

    try (ZipFile zipFile = new ZipFile(archiveFilePath)) {
      final Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        final String entryName = entries.nextElement().getName();

        if (FlashBuilderImporter.DOT_PROJECT.equals(entryName)) containsDotProjectFile = true;
        if (FlashBuilderImporter.DOT_ACTION_SCRIPT_PROPERTIES.equals(entryName)) containsDotActionScriptFile = true;

        if (entryName.endsWith(FlashBuilderImporter.DOT_FXP) || entryName.endsWith(FlashBuilderImporter.DOT_FXPL)) {
          containsNestedProjects = true;
        }

        if (containsDotProjectFile && containsDotActionScriptFile && containsNestedProjects) break;
      }

      if (!containsDotProjectFile || !containsDotActionScriptFile) {
        throw new ConfigurationException(FlexBundle.message("does.not.contain.flash.builder.projects"), CommonBundle.getErrorTitle());
      }

      return containsNestedProjects;
    }
    catch (IOException e) {
      throw new ConfigurationException(FlexBundle.message("does.not.contain.flash.builder.projects"), CommonBundle.getErrorTitle());
    }
  }
}
