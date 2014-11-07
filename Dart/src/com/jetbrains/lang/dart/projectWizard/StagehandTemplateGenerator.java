package com.jetbrains.lang.dart.projectWizard;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.projectWizard.Stagehand.StagehandException;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StagehandTemplateGenerator extends DartEmptyProjectGenerator {

  private static final String LOWER_CASE_APP_SUFFIX = "app";
  private static final String UPPER_CASE_APP_SUFFIX = " App";

  @NotNull private final Stagehand myStagehand;
  @NotNull private final String myId;
  @NotNull private final String myEntrypoint;

  public StagehandTemplateGenerator(@NotNull final Stagehand stagehand, @NotNull final String id, @NotNull final String description, @NotNull final String entrypoint) {
    super(prettify(id), description);
    myStagehand = stagehand;
    myId = id;
    myEntrypoint = entrypoint;
  }

  private static String prettify(String name) {
    // consoleapp -> Console App
    name = name.substring(0, 1).toUpperCase() + name.substring(1);
    if (name.endsWith(LOWER_CASE_APP_SUFFIX)) {
      name = name.substring(0, name.length() - LOWER_CASE_APP_SUFFIX.length()) + UPPER_CASE_APP_SUFFIX;
    }
    return name;
  }

  @NotNull
  protected VirtualFile[] doGenerateProject(final Project project, final Module module, final VirtualFile baseDir) throws IOException {

    try {
      final String path = baseDir.getCanonicalPath();
      if (path == null) {
        throw new IOException(DartBundle.message("dart.pub.stagehand.exception.invalid.basedir.path"));
      }
      myStagehand.generateInto(new File(path), myId);
    }
    catch (StagehandException e) {
      throw new IOException(e);
    }

    List<VirtualFile> projectFiles = new ArrayList<VirtualFile>();

    addIfExists(projectFiles, project, myEntrypoint);
    addIfExists(projectFiles, project, PubspecYamlUtil.PUBSPEC_YAML);

    return projectFiles.toArray(VirtualFile.EMPTY_ARRAY);
  }


  private void addIfExists(final List<VirtualFile> files, final Project project, final String filePath) {
    final VirtualFile file = project.getBaseDir().findFileByRelativePath(filePath);
    if (file != null  && file.exists()) {
      files.add(file);
    }
  }

}
