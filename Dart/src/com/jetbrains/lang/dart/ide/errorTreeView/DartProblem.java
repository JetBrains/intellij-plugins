package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.util.DartBuildFileUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.dartlang.analysis.server.protocol.AnalysisError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class DartProblem {

  @NotNull private final Project myProject;
  @NotNull private final AnalysisError myAnalysisError;

  private String mySystemIndependentPath;

  @Nullable private VirtualFile myFile;
  @Nullable private VirtualFile myPackageRoot;
  @Nullable private VirtualFile myContentRoot;
  private String myPresentableLocationWithoutLineNumber;

  public DartProblem(@NotNull final Project project, @NotNull final AnalysisError error) {
    myProject = project;
    myAnalysisError = error;
  }

  @NotNull
  public String getErrorMessage() {
    return myAnalysisError.getMessage();
  }

  public String getSeverity() {
    return myAnalysisError.getSeverity();
  }

  public int getLineNumber() {
    return myAnalysisError.getLocation().getStartLine();
  }

  public int getOffset() {
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(mySystemIndependentPath);
    return DartAnalysisServerService.getInstance(myProject).getConvertedOffset(file, myAnalysisError.getLocation().getOffset());
  }

  @NotNull
  public String getSystemIndependentPath() {
    if (mySystemIndependentPath == null) {
      mySystemIndependentPath = FileUtil.toSystemIndependentName(myAnalysisError.getLocation().getFile());
    }
    return mySystemIndependentPath;
  }

  private void ensureInitialized() {
    if (myPresentableLocationWithoutLineNumber != null) return;

    // temporary final vars guarantee that vars are initialized before this method exits
    final VirtualFile file;
    final String dartPackageName;
    final String presentableFilePath;
    final VirtualFile packageRoot;
    final VirtualFile contentRoot;

    file = LocalFileSystem.getInstance().findFileByPath(getSystemIndependentPath());
    if (file == null) {
      dartPackageName = null;
      packageRoot = null;
      contentRoot = null;
      presentableFilePath = myAnalysisError.getLocation().getFile();
    }
    else {
      contentRoot = ProjectRootManager.getInstance(myProject).getFileIndex().getContentRootForFile(file, false);

      final VirtualFile pubspec = Registry.is("dart.projects.without.pubspec", false)
                                  ? DartBuildFileUtil.findPackageRootBuildFile(myProject, file)
                                  : PubspecYamlUtil.findPubspecYamlFile(myProject, file);
      if (pubspec == null) {
        dartPackageName = null;
        if (contentRoot == null) {
          packageRoot = null;
          presentableFilePath = myAnalysisError.getLocation().getFile();
        }
        else {
          packageRoot = contentRoot;
          final String relativePath = VfsUtilCore.getRelativePath(file, contentRoot, File.separatorChar);
          presentableFilePath = relativePath != null ? relativePath : myAnalysisError.getLocation().getFile();
        }
      }
      else {
        final String projectName = Registry.is("dart.projects.without.pubspec", false)
                                   ? DartBuildFileUtil.getDartProjectName(pubspec)
                                   : PubspecYamlUtil.getDartProjectName(pubspec);
        dartPackageName = projectName != null ? projectName : "%unnamed%";
        packageRoot = pubspec.getParent();
        final String relativePath = VfsUtilCore.getRelativePath(file, pubspec.getParent(), File.separatorChar);
        presentableFilePath = relativePath != null ? relativePath : myAnalysisError.getLocation().getFile();
      }
    }

    myFile = file;
    myPackageRoot = packageRoot;
    myContentRoot = contentRoot;
    myPresentableLocationWithoutLineNumber = dartPackageName == null ? presentableFilePath
                                                                     : ("[" + dartPackageName + "] " + presentableFilePath);
  }


  /**
   * Returns Dart package name in brackets and relative path form Dart package root to the file.
   * If no pubspec.yaml then returns relative part from content root to the file.
   * File path is returned as failover.
   */
  @NotNull
  public String getPresentableLocationWithoutLineNumber() {
    ensureInitialized();
    return myPresentableLocationWithoutLineNumber;
  }

  @NotNull
  public String getPresentableLocation() {
    return getPresentableLocationWithoutLineNumber() + ":" + getLineNumber();
  }

  @Nullable
  public VirtualFile getFile() {
    ensureInitialized();
    return myFile;
  }

  @Nullable
  public VirtualFile getPackageRoot() {
    ensureInitialized();
    return myPackageRoot;
  }

  @Nullable
  public VirtualFile getContentRoot() {
    ensureInitialized();
    return myContentRoot;
  }
}
