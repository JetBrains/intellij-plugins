// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.projectWizard;

import com.intellij.execution.ExecutionException;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class StagehandTemplate extends DartProjectTemplate {
  private final @NotNull Stagehand myStagehand;
  private final @NotNull Stagehand.StagehandDescriptor myTemplate;

  StagehandTemplate(final @NotNull Stagehand stagehand, final @NotNull Stagehand.StagehandDescriptor template) {
    super(template.myLabel, template.myDescription);
    myStagehand = stagehand;
    myTemplate = template;
  }

  @Override
  public Collection<VirtualFile> generateProject(final @NotNull String sdkRoot,
                                                 final @NotNull Module module,
                                                 final @NotNull VirtualFile baseDir) throws IOException {
    try {
      myStagehand.generateInto(sdkRoot, baseDir, myTemplate.myId);
    }
    catch (ExecutionException e) {
      throw new IOException(e);
    }

    final List<VirtualFile> files = new ArrayList<>();

    final VirtualFile pubspec =
      LocalFileSystem.getInstance().refreshAndFindFileByPath(baseDir.getPath() + "/" + PubspecYamlUtil.PUBSPEC_YAML);
    ContainerUtil.addIfNotNull(files, pubspec);

    // template entrypoint is usually like "bin/__projectName__.dart"
    String entrypoint = myTemplate.myEntrypoint;
    if (pubspec != null && entrypoint != null) {
      String projectName = PubspecYamlUtil.getDartProjectName(pubspec);
      if (projectName != null) {
        @NonNls String projectNamePlaceholder = "__projectName__";
        entrypoint = StringUtil.replace(entrypoint, projectNamePlaceholder, projectName);
      }
    }

    final VirtualFile mainFile =
      StringUtil.isEmpty(entrypoint) ? null
                                     : LocalFileSystem.getInstance().refreshAndFindFileByPath(baseDir.getPath() + "/" + entrypoint);

    if (mainFile != null && mainFile.getName().equals("index.html")) {
      ContainerUtil.addIfNotNull(files, mainFile.getParent().findChild("main.dart"));
    }

    ContainerUtil.addIfNotNull(files, mainFile);

    VirtualFile testFolder = LocalFileSystem.getInstance().refreshAndFindFileByPath(baseDir.getPath() + "/test");
    if (testFolder != null && testFolder.isDirectory()) {
      createTestRunConfiguration(module, baseDir.getPath());
    }

    if (!StringUtil.isEmpty(entrypoint) && mainFile != null) {
      if (entrypoint.startsWith("bin/") && FileTypeRegistry.getInstance().isFileOfType(mainFile, DartFileType.INSTANCE)) {
        createCmdLineRunConfiguration(module, mainFile);
      }
      if (entrypoint.startsWith("web/") && FileTypeRegistry.getInstance().isFileOfType(mainFile, HtmlFileType.INSTANCE)) {
        createWebRunConfiguration(module, mainFile);
      }
    }

    return files;
  }
}
