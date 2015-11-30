package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
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
    return myAnalysisError.getLocation().getOffset();
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

    file = LocalFileSystem.getInstance().findFileByPath(getSystemIndependentPath());
    if (file == null) {
      dartPackageName = null;
      packageRoot = null;
      presentableFilePath = myAnalysisError.getLocation().getFile();
    }
    else {
      final VirtualFile pubspec = PubspecYamlUtil.findPubspecYamlFile(myProject, file);
      if (pubspec == null) {
        dartPackageName = null;
        final VirtualFile contentRoot = ProjectRootManager.getInstance(myProject).getFileIndex().getContentRootForFile(file, false);
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
        final String projectName = PubspecYamlUtil.getDartProjectName(pubspec);
        dartPackageName = projectName != null ? projectName : "%unnamed%";
        packageRoot = pubspec.getParent();
        final String relativePath = VfsUtilCore.getRelativePath(file, pubspec.getParent(), File.separatorChar);
        presentableFilePath = relativePath != null ? relativePath : myAnalysisError.getLocation().getFile();
      }
    }

    myFile = file;
    myPackageRoot = packageRoot;
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
}
