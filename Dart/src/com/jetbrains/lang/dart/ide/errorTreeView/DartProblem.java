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
  private String myPresentableFilePath;
  private String myDartPackageName;
  private String myTextWithoutLineNumber;

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

  /**
   * Returns relative path form Dart package root to the file.
   * If no pubspec.yaml then returns relative part from content root to the file.
   * File path is returned as failover.
   */
  @NotNull
  public String getPresentableFilePath() {
    if (myPresentableFilePath != null) {
      return myPresentableFilePath;
    }

    // temporary final vars guarantee that both vars are initialized before this method exits
    final String dartPackageName;
    final String presentableFilePath;

    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(getSystemIndependentPath());
    if (file == null) {
      dartPackageName = null;
      presentableFilePath = myAnalysisError.getLocation().getFile();
    }
    else {
      final VirtualFile pubspec = PubspecYamlUtil.findPubspecYamlFile(myProject, file);
      if (pubspec == null) {
        dartPackageName = null;
        final VirtualFile contentRoot = ProjectRootManager.getInstance(myProject).getFileIndex().getContentRootForFile(file, false);
        if (contentRoot == null) {
          presentableFilePath = myAnalysisError.getLocation().getFile();
        }
        else {
          final String relativePath = VfsUtilCore.getRelativePath(file, contentRoot, File.separatorChar);
          presentableFilePath = relativePath != null ? relativePath : myAnalysisError.getLocation().getFile();
        }
      }
      else {
        final String projectName = PubspecYamlUtil.getDartProjectName(pubspec);
        dartPackageName = projectName != null ? projectName : "%unnamed%";
        final String relativePath = VfsUtilCore.getRelativePath(file, pubspec.getParent(), File.separatorChar);
        presentableFilePath = relativePath != null ? relativePath : myAnalysisError.getLocation().getFile();
      }
    }

    myDartPackageName = dartPackageName;
    myPresentableFilePath = presentableFilePath;
    return myPresentableFilePath;
  }

  @Nullable
  public String getDartPackageName() {
    getPresentableFilePath(); // ensure initialized
    return myDartPackageName;
  }

  @NotNull
  public String getPresentableLocationWithoutLineNumber() {
    if (myTextWithoutLineNumber == null) {
      final String packageName = getDartPackageName();
      myTextWithoutLineNumber = packageName == null ? getPresentableFilePath()
                                                    : ("[" + packageName + "] " + getPresentableFilePath());
    }
    return myTextWithoutLineNumber;
  }

  @NotNull
  public String getPresentableLocation() {
    return getPresentableLocationWithoutLineNumber() + ":" + getLineNumber();
  }
}
